#!/usr/bin/env python3
"""
scan_project.py

Generate factual scan files for the `mydrugs` codebase.

This script is intentionally deterministic and non-LLM:
- It does not call Gradle.
- It does not compile the project.
- It does not modify source files.
- It only reads the repository and writes Markdown/text indexes under `scan/`.

Usage:
    python scripts/scan_project.py
    python scripts/scan_project.py --root .
    python scripts/scan_project.py --root /path/to/repo --out-dir scan
    python scripts/scan_project.py --quiet

Recommended:
    Put this file at `scripts/scan_project.py`, then run it from the repo root.
"""

from __future__ import annotations

import argparse
import datetime as _dt
import json
import re
import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Iterator


EXCLUDED_DIRS = {
    ".git",
    ".gradle",
    ".idea",
    ".vscode",
    "build",
    "out",
    "target",
    ".settings",
    ".classpath",
    ".project",
    "node_modules",
}

TEXT_EXTENSIONS = {
    ".java",
    ".json",
    ".mcmeta",
    ".md",
    ".txt",
    ".toml",
    ".properties",
    ".gradle",
    ".kts",
    ".cfg",
    ".yaml",
    ".yml",
}


@dataclass(frozen=True)
class Match:
    path: Path
    line_no: int
    line: str


def rel(path: Path, root: Path) -> str:
    try:
        return path.relative_to(root).as_posix()
    except ValueError:
        return path.as_posix()


def is_excluded(path: Path) -> bool:
    return any(part in EXCLUDED_DIRS for part in path.parts)


def is_text_file(path: Path) -> bool:
    if path.suffix in TEXT_EXTENSIONS:
        return True
    if path.name in {"build.gradle", "settings.gradle", "gradle.properties", "gradlew"}:
        return True
    return False


def read_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8", errors="replace")
    except Exception as exc:
        return f"<<ERROR READING FILE: {exc}>>"


def iter_files(root: Path, *bases: str, suffixes: set[str] | None = None) -> list[Path]:
    files: list[Path] = []
    for base in bases:
        start = root / base
        if not start.exists():
            continue
        for path in start.rglob("*"):
            if path.is_file() and not is_excluded(path):
                if suffixes is None or path.suffix in suffixes:
                    files.append(path)
    return sorted(files, key=lambda p: rel(p, root))


def iter_text_files(root: Path, *bases: str) -> list[Path]:
    return [p for p in iter_files(root, *bases) if is_text_file(p)]


def iter_lines(path: Path) -> Iterator[tuple[int, str]]:
    text = read_text(path)
    for i, line in enumerate(text.splitlines(), start=1):
        yield i, line.rstrip("\n")


def grep_files(files: Iterable[Path], pattern: str | re.Pattern[str], *, flags: int = 0) -> list[Match]:
    rx = re.compile(pattern, flags) if isinstance(pattern, str) else pattern
    matches: list[Match] = []
    for path in files:
        for line_no, line in iter_lines(path):
            if rx.search(line):
                matches.append(Match(path, line_no, line.strip()))
    return matches


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.rstrip() + "\n", encoding="utf-8")


def md_match_list(title: str, matches: list[Match], root: Path, note: str | None = None) -> str:
    lines = [f"# {title}", ""]
    if note:
        lines += [note, ""]
    if not matches:
        lines += ["No matches found.", ""]
        return "\n".join(lines)

    grouped: dict[str, list[Match]] = defaultdict(list)
    for m in matches:
        grouped[rel(m.path, root)].append(m)

    for file_path in sorted(grouped):
        lines += [f"## `{file_path}`", ""]
        for m in grouped[file_path]:
            clean = m.line.replace("`", "\\`")
            lines.append(f"- L{m.line_no}: `{clean}`")
        lines.append("")

    return "\n".join(lines)


def compact_tree(root: Path, starts: list[str], max_depth: int = 8) -> str:
    lines: list[str] = ["# Compact Project Tree", ""]

    def walk_dir(path: Path, prefix: str = "", depth: int = 0) -> None:
        if depth > max_depth:
            lines.append(prefix + "…")
            return

        try:
            children = sorted(
                [p for p in path.iterdir() if not is_excluded(p)],
                key=lambda p: (p.is_file(), p.name.lower()),
            )
        except Exception as exc:
            lines.append(prefix + f"<<error reading dir: {exc}>>")
            return

        for idx, child in enumerate(children):
            connector = "└── " if idx == len(children) - 1 else "├── "
            next_prefix = prefix + ("    " if idx == len(children) - 1 else "│   ")
            display = child.name + ("/" if child.is_dir() else "")
            lines.append(prefix + connector + display)
            if child.is_dir():
                walk_dir(child, next_prefix, depth + 1)

    for start in starts:
        path = root / start
        if not path.exists():
            continue
        lines += [f"## `{start}`", ""]
        lines.append(path.name + "/")
        walk_dir(path)
        lines.append("")

    return "\n".join(lines)


def package_index(root: Path, java_files: list[Path]) -> str:
    packages: dict[str, list[str]] = defaultdict(list)

    for path in java_files:
        text = read_text(path)
        match = re.search(r"^\s*package\s+([\w.]+)\s*;", text, re.MULTILINE)
        package_name = match.group(1) if match else "<<no package declaration>>"
        packages[package_name].append(rel(path, root))

    lines = ["# Package Index", ""]
    if not packages:
        lines += ["No Java packages found.", ""]
        return "\n".join(lines)

    for package_name in sorted(packages):
        lines += [f"## `{package_name}`", ""]
        for file_path in sorted(packages[package_name]):
            lines.append(f"- `{file_path}`")
        lines.append("")

    return "\n".join(lines)


def symbol_index(root: Path, java_files: list[Path]) -> str:
    type_rx = re.compile(
        r"^\s*(?:public|protected|private|abstract|final|sealed|non-sealed|static|\s)*"
        r"(class|record|enum|interface|@interface)\s+([A-Za-z_]\w*)"
    )
    method_rx = re.compile(
        r"^\s*(?:public|protected|private|static|final|abstract|synchronized|native|default|\s)+"
        r"(?:<[^>]+>\s*)?"
        r"[\w.$<>\[\], ?&]+\s+([A-Za-z_]\w*)\s*\([^;]*\)\s*(?:throws [^{]+)?\{?\s*$"
    )
    constructor_rx = re.compile(
        r"^\s*(?:public|protected|private)\s+([A-Z][A-Za-z_0-9]*)\s*\([^;]*\)\s*(?:throws [^{]+)?\{?\s*$"
    )

    lines = ["# Symbol Index", ""]
    if not java_files:
        lines += ["No Java files found.", ""]
        return "\n".join(lines)

    for path in java_files:
        declarations: list[str] = []
        methods: list[str] = []

        for line_no, line in iter_lines(path):
            type_match = type_rx.search(line)
            if type_match:
                declarations.append(f"- L{line_no}: {type_match.group(1)} `{type_match.group(2)}`")
                continue

            method_match = method_rx.search(line)
            if method_match:
                name = method_match.group(1)
                if name not in {"if", "for", "while", "switch", "catch", "return", "new"}:
                    methods.append(f"- L{line_no}: `{name}(...)`")
                continue

            constructor_match = constructor_rx.search(line)
            if constructor_match:
                methods.append(f"- L{line_no}: constructor `{constructor_match.group(1)}(...)`")

        if declarations or methods:
            lines += [f"## `{rel(path, root)}`", ""]
            if declarations:
                lines += ["### Types", *declarations, ""]
            if methods:
                lines += ["### Method-like declarations", *methods, ""]
            lines.append("")

    return "\n".join(lines)


def registry_index(root: Path, java_files: list[Path]) -> str:
    patterns = {
        "Registry infrastructure": r"DeferredRegister|DeferredHolder|RegistryObject|RegisterEvent|Registry\.|BuiltInRegistries",
        "Likely item registries": r"\bModItems\b|ITEMS\s*=|DeferredRegister\.Items|registerItem|new Item\(",
        "Likely block registries": r"\bModBlocks\b|BLOCKS\s*=|DeferredRegister\.Blocks|registerBlock|new Block\(",
        "Likely fluid registries": r"\bModFluids\b|FLUIDS\s*=|DeferredRegister\.Fluids|FlowingFluid|FluidType",
        "Likely block entity registries": r"\bModBlockEntities\b|BLOCK_ENTIT|BlockEntityType",
        "Likely menu registries": r"\bModMenus\b|MenuType|IContainerFactory",
        "Likely effect registries": r"\bModEffects\b|MobEffect|MobEffectInstance",
        "Likely creative tabs": r"\bModCreativeTabs\b|CreativeModeTab",
        "Likely data components": r"DataComponentType|DataComponents|component\(",
    }

    lines = ["# Registry Index", ""]
    for section, pattern in patterns.items():
        matches = grep_files(java_files, pattern)
        lines += [f"## {section}", ""]
        if matches:
            for m in matches:
                clean = m.line.replace("`", "\\`")
                lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
        else:
            lines.append("No matches found.")
        lines.append("")

    return "\n".join(lines)


def network_index(root: Path, java_files: list[Path]) -> str:
    patterns = {
        "Payload declarations and codecs": r"CustomPacketPayload|StreamCodec|ByteBufCodecs|Type<|ResourceLocation\.fromNamespaceAndPath",
        "Payload registration": r"registerPayload|register\(|PayloadRegistrar|playToServer|playToClient|commonToClient|commonToServer",
        "Server handlers": r"ServerPayloadHandlers|handle\(|ServerPlayer|IPayloadContext|enqueueWork",
        "Client handlers": r"ClientPayloadHandlers|Minecraft\.getInstance|Dist\.CLIENT|client",
        "Menu / screen validation hints": r"menuId|stillValid|containerId|AbstractContainerMenu|MenuProvider|BlockPos|hand|InteractionHand",
        "Numeric bounds / validation hints": r"NaN|isNaN|isFinite|Double\.is|Float\.is|clamp|Mth\.clamp|Math\.max|Math\.min",
        "Permission / debug gates": r"hasPermissions|permission|debug|admin|server config|Config\.SERVER",
        "Potential codec risks": r"\.ordinal\(|ByteBufCodecs\.STRING_UTF8|Utf8|Enum|byId|fromId",
    }

    lines = ["# Network Payload Index", ""]
    lines += [
        "This file is generated from text patterns. It identifies candidates, not proven bugs.",
        "",
    ]

    for section, pattern in patterns.items():
        matches = grep_files(java_files, pattern)
        lines += [f"## {section}", ""]
        if matches:
            for m in matches:
                clean = m.line.replace("`", "\\`")
                lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
        else:
            lines.append("No matches found.")
        lines.append("")

    return "\n".join(lines)


def client_server_scan(root: Path, java_files: list[Path]) -> str:
    forbidden = re.compile(
        r"^\s*import\s+(net\.minecraft\.client|net\.neoforged\.neoforge\.client|org\.mydrugs\.mydrugs\.client)\."
    )
    violations: list[Match] = []
    client_path_token = "/client/"

    for path in java_files:
        rel_path = "/" + rel(path, root)
        if client_path_token in rel_path:
            continue
        for line_no, line in iter_lines(path):
            if forbidden.search(line):
                violations.append(Match(path, line_no, line.strip()))

    status = "FAIL" if violations else "PASS"
    lines = ["# Client/Server Boundary Scan", "", f"Status: **{status}**", ""]
    lines += [
        "Forbidden imports outside `src/main/java/org/mydrugs/mydrugs/client/**`:",
        "",
        "- `net.minecraft.client.*`",
        "- `net.neoforged.neoforge.client.*`",
        "- `org.mydrugs.mydrugs.client.*`",
        "",
    ]

    if not violations:
        lines.append("No forbidden imports found outside client packages.")
        lines.append("")
        return "\n".join(lines)

    lines += ["## Violations", ""]
    for m in violations:
        clean = m.line.replace("`", "\\`")
        lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
    lines.append("")

    return "\n".join(lines)


def component_literal_report(root: Path, java_files: list[Path]) -> str:
    matches = grep_files(java_files, r"Component\.literal\s*\(")
    lines = [
        "# Component Literal Report",
        "",
        "This is a generated candidate list. It does not prove a localization bug.",
        "",
        "Review rule:",
        "",
        "- Player-facing text should usually use `Component.translatable`.",
        "- `Component.literal` may be okay for debug text, dynamic numeric fragments, internal labels, or temporary diagnostics.",
        "",
    ]

    if not matches:
        lines += ["No `Component.literal(` matches found.", ""]
        return "\n".join(lines)

    grouped: dict[str, list[Match]] = defaultdict(list)
    for m in matches:
        grouped[rel(m.path, root)].append(m)

    lines += ["## Raw matches", ""]
    for file_path in sorted(grouped):
        lines += [f"### `{file_path}`", ""]
        for m in grouped[file_path]:
            clean = m.line.replace("`", "\\`")
            lines.append(f"- L{m.line_no}: `{clean}`")
        lines.append("")

    return "\n".join(lines)


def todos_report(root: Path, files: list[Path]) -> str:
    matches = grep_files(files, r"\b(TODO|FIXME|HACK|XXX|temporary|legacy)\b", flags=re.IGNORECASE)
    return md_match_list(
        "TODO / FIXME / Legacy Index",
        matches,
        root,
        note="Generated candidate list from Java, resources, and docs.",
    )


def resources_index(root: Path) -> str:
    main = root / "src/main/resources"
    generated = root / "src/generated/resources"

    categories = [
        "assets/mydrugs/lang",
        "assets/mydrugs/models",
        "assets/mydrugs/blockstates",
        "assets/mydrugs/textures",
        "assets/mydrugs/guide",
        "assets/mydrugs/sounds.json",
        "data/mydrugs/recipe",
        "data/mydrugs/recipes",
        "data/mydrugs/loot_table",
        "data/mydrugs/loot_tables",
        "data/mydrugs/tags",
        "data/minecraft/tags",
    ]

    lines = ["# Resource Index", ""]
    for base_name, base in [("Hand-authored resources", main), ("Generated resources", generated)]:
        lines += [f"## {base_name}", ""]
        if not base.exists():
            lines += [f"`{rel(base, root)}` does not exist.", ""]
            continue

        for category in categories:
            path = base / category
            if path.exists():
                files = sorted([p for p in path.rglob("*") if p.is_file()], key=lambda p: rel(p, root))
                lines += [f"### `{rel(path, root)}`", ""]
                if files:
                    for file in files:
                        lines.append(f"- `{rel(file, root)}`")
                else:
                    lines.append("Directory exists but contains no files.")
                lines.append("")

    return "\n".join(lines)


def generated_vs_authored(root: Path) -> str:
    main_root = root / "src/main/resources"
    gen_root = root / "src/generated/resources"
    main_files = {
        p.relative_to(main_root).as_posix(): p
        for p in iter_files(root, "src/main/resources")
        if main_root.exists()
    }
    gen_files = {
        p.relative_to(gen_root).as_posix(): p
        for p in iter_files(root, "src/generated/resources")
        if gen_root.exists()
    }

    overlap = sorted(set(main_files) & set(gen_files))
    only_main = sorted(set(main_files) - set(gen_files))
    only_gen = sorted(set(gen_files) - set(main_files))

    lines = ["# Generated vs Hand-Authored Resources", ""]
    lines += [
        "This compares paths under `src/main/resources` and `src/generated/resources`.",
        "Overlaps may indicate drift or intentional overrides that need review.",
        "",
    ]

    lines += ["## Overlapping relative paths", ""]
    if overlap:
        for item in overlap:
            lines.append(f"- `{item}`")
    else:
        lines.append("No overlapping relative paths found.")
    lines.append("")

    lines += ["## Hand-authored only", ""]
    for item in only_main:
        lines.append(f"- `{item}`")
    if not only_main:
        lines.append("None.")
    lines.append("")

    lines += ["## Generated only", ""]
    for item in only_gen:
        lines.append(f"- `{item}`")
    if not only_gen:
        lines.append("None.")
    lines.append("")

    return "\n".join(lines)


def localization_index(root: Path, java_files: list[Path]) -> str:
    lang_files = []
    for base in ["src/main/resources/assets/mydrugs/lang", "src/generated/resources/assets/mydrugs/lang"]:
        lang_files.extend(iter_files(root, base, suffixes={".json"}))

    keys: dict[str, list[str]] = defaultdict(list)
    json_errors: list[str] = []

    for path in lang_files:
        try:
            data = json.loads(read_text(path))
            if isinstance(data, dict):
                for key in sorted(data):
                    keys[key].append(rel(path, root))
            else:
                json_errors.append(f"`{rel(path, root)}`: root is not a JSON object")
        except Exception as exc:
            json_errors.append(f"`{rel(path, root)}`: {exc}")

    translatable = grep_files(java_files, r"Component\.translatable\s*\(|Util\.makeDescriptionId|descriptionId|getDescriptionId")
    literals = grep_files(java_files, r"Component\.literal\s*\(")

    lines = ["# Localization Index", ""]
    lines += ["## Lang files", ""]
    if lang_files:
        for path in sorted(lang_files, key=lambda p: rel(p, root)):
            lines.append(f"- `{rel(path, root)}`")
    else:
        lines.append("No lang JSON files found.")
    lines.append("")

    if json_errors:
        lines += ["## JSON parse errors", ""]
        lines.extend(f"- {err}" for err in json_errors)
        lines.append("")

    lines += ["## Lang keys", ""]
    if keys:
        for key in sorted(keys):
            locations = ", ".join(f"`{p}`" for p in sorted(keys[key]))
            lines.append(f"- `{key}` — {locations}")
    else:
        lines.append("No language keys extracted.")
    lines.append("")

    lines += ["## Translatable / description-id call sites", ""]
    if translatable:
        for m in translatable:
            clean = m.line.replace("`", "\\`")
            lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
    else:
        lines.append("No translatable/description-id call sites found.")
    lines.append("")

    lines += ["## Literal call sites", ""]
    lines += [f"See `scan/component_literal_report.md` for {len(literals)} raw matches.", ""]
    return "\n".join(lines)


def datagen_index(root: Path, java_files: list[Path]) -> str:
    patterns = {
        "Datagen provider candidates": r"DataProvider|PackOutput|DatapackBuiltinEntriesProvider|RecipeProvider|LootTableProvider|BlockStateProvider|ItemModelProvider|LanguageProvider|TagsProvider|GatherDataEvent|DataGenerator",
        "Generated resource references": r"src/generated/resources|ExistingFileHelper|runData|includeServer|includeClient|addProvider",
        "Recipe datagen": r"RecipeProvider|RecipeOutput|ShapedRecipeBuilder|ShapelessRecipeBuilder|SimpleCookingRecipeBuilder",
        "Loot datagen": r"LootTableProvider|BlockLootSubProvider|LootTable\.Builder|dropSelf|createSingleItemTable",
        "Model/blockstate datagen": r"BlockStateProvider|ItemModelProvider|ModelProvider|simpleBlock|simpleBlockItem|itemModels",
        "Tag datagen": r"TagsProvider|IntrinsicHolderTagsProvider|tag\(",
        "Language datagen": r"LanguageProvider|add\(",
    }

    lines = ["# Datagen Index", ""]
    lines += [
        "This file is generated from Java text patterns. It identifies provider candidates and drift-sensitive areas.",
        "",
    ]

    for section, pattern in patterns.items():
        matches = grep_files(java_files, pattern)
        lines += [f"## {section}", ""]
        if matches:
            for m in matches:
                clean = m.line.replace("`", "\\`")
                lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
        else:
            lines.append("No matches found.")
        lines.append("")

    return "\n".join(lines)


def recipes_index(root: Path, java_files: list[Path]) -> str:
    recipe_files = [
        p for p in iter_files(root, "src/main/resources", "src/generated/resources", suffixes={".json"})
        if "/recipe/" in "/" + rel(p, root) or "/recipes/" in "/" + rel(p, root)
    ]
    provider_matches = grep_files(java_files, r"RecipeProvider|RecipeOutput|ShapedRecipeBuilder|ShapelessRecipeBuilder|recipe")
    lines = ["# Recipe Index", "", "## Recipe JSON files", ""]
    if recipe_files:
        for p in recipe_files:
            lines.append(f"- `{rel(p, root)}`")
    else:
        lines.append("No recipe JSON files found.")
    lines += ["", "## Recipe provider / code candidates", ""]
    if provider_matches:
        for m in provider_matches:
            clean = m.line.replace("`", "\\`")
            lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
    else:
        lines.append("No recipe provider/code candidates found.")
    lines.append("")
    return "\n".join(lines)


def tags_index(root: Path, java_files: list[Path]) -> str:
    tag_files = [
        p for p in iter_files(root, "src/main/resources", "src/generated/resources", suffixes={".json"})
        if "/tags/" in "/" + rel(p, root)
    ]
    provider_matches = grep_files(java_files, r"TagsProvider|TagKey|tag\(|Tags\.")
    lines = ["# Tag Index", "", "## Tag JSON files", ""]
    if tag_files:
        for p in tag_files:
            lines.append(f"- `{rel(p, root)}`")
    else:
        lines.append("No tag JSON files found.")
    lines += ["", "## Tag provider / code candidates", ""]
    if provider_matches:
        for m in provider_matches:
            clean = m.line.replace("`", "\\`")
            lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
    else:
        lines.append("No tag provider/code candidates found.")
    lines.append("")
    return "\n".join(lines)


def loot_tables_index(root: Path, java_files: list[Path]) -> str:
    loot_files = [
        p for p in iter_files(root, "src/main/resources", "src/generated/resources", suffixes={".json"})
        if "/loot_table/" in "/" + rel(p, root) or "/loot_tables/" in "/" + rel(p, root)
    ]
    provider_matches = grep_files(java_files, r"LootTableProvider|BlockLootSubProvider|LootTable|dropSelf|createSingleItemTable")
    lines = ["# Loot Table Index", "", "## Loot table JSON files", ""]
    if loot_files:
        for p in loot_files:
            lines.append(f"- `{rel(p, root)}`")
    else:
        lines.append("No loot table JSON files found.")
    lines += ["", "## Loot provider / code candidates", ""]
    if provider_matches:
        for m in provider_matches:
            clean = m.line.replace("`", "\\`")
            lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
    else:
        lines.append("No loot provider/code candidates found.")
    lines.append("")
    return "\n".join(lines)


def models_and_blockstates_index(root: Path) -> str:
    files = [
        p for p in iter_files(root, "src/main/resources", "src/generated/resources", suffixes={".json"})
        if "/models/" in "/" + rel(p, root) or "/blockstates/" in "/" + rel(p, root)
    ]

    lines = ["# Models and Blockstates Index", ""]
    grouped: dict[str, list[Path]] = defaultdict(list)
    for p in files:
        path_str = rel(p, root)
        if "/blockstates/" in "/" + path_str:
            grouped["Blockstates"].append(p)
        elif "/models/block/" in "/" + path_str:
            grouped["Block models"].append(p)
        elif "/models/item/" in "/" + path_str:
            grouped["Item models"].append(p)
        else:
            grouped["Other models"].append(p)

    if not files:
        lines += ["No model or blockstate JSON files found.", ""]
        return "\n".join(lines)

    for section in ["Blockstates", "Block models", "Item models", "Other models"]:
        lines += [f"## {section}", ""]
        if grouped.get(section):
            for p in sorted(grouped[section], key=lambda p: rel(p, root)):
                lines.append(f"- `{rel(p, root)}`")
        else:
            lines.append("No files found.")
        lines.append("")

    return "\n".join(lines)


def guide_references(root: Path) -> str:
    guide_files = []
    possible = [
        root / "docs/progression_guide_pages.md",
        root / "src/main/resources/assets/mydrugs/guide/pages.json",
        root / "src/generated/resources/assets/mydrugs/guide/pages.json",
    ]
    guide_files.extend([p for p in possible if p.exists()])

    for p in iter_files(root, "docs", "src/main/resources/assets/mydrugs/guide", "src/generated/resources/assets/mydrugs/guide"):
        if p not in guide_files and p.suffix in {".md", ".json", ".txt"}:
            guide_files.append(p)

    patterns = {
        "Item references": r"@item|mydrugs:[A-Za-z0-9_./-]+",
        "Block references": r"@block",
        "Effect references": r"@effect",
        "Recipe references": r"@recipe",
        "Other guide markers": r"@[A-Za-z_]+",
    }

    lines = ["# Guide References Index", ""]
    lines += [
        "This file extracts references from guide-related files.",
        "It does not rewrite or validate the guide by itself.",
        "",
    ]

    if not guide_files:
        lines += ["No guide files found.", ""]
        return "\n".join(lines)

    lines += ["## Guide files scanned", ""]
    unique_guide_files = sorted(set(guide_files), key=lambda p: rel(p, root))
    for p in unique_guide_files:
        lines.append(f"- `{rel(p, root)}`")
    lines.append("")

    for section, pattern in patterns.items():
        matches = grep_files(unique_guide_files, pattern)
        lines += [f"## {section}", ""]
        if matches:
            for m in matches:
                clean = m.line.replace("`", "\\`")
                lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
        else:
            lines.append("No matches found.")
        lines.append("")

    return "\n".join(lines)


def performance_hotspots(root: Path, java_files: list[Path]) -> str:
    patterns = {
        "Tick candidates": r"\btick\s*\(|onClientTick|onServerTick|PlayerTickEvent|LevelTickEvent|ServerTickEvent|ClientTickEvent",
        "Render-frame candidates": r"RenderGuiEvent|RenderLevelStageEvent|RenderTickEvent|render\s*\(|PoseStack|GuiGraphics",
        "Entity / world scan candidates": r"getEntities|getEntitiesOfClass|getBlockStates|BlockPos\.betweenClosed|AABB|inflate\(|level\.players\(",
        "Loop candidates in risky contexts": r"\bwhile\s*\(|\bfor\s*\(",
        "Scheduling/cache candidates": r"dirty|cache|cached|schedule|interval|cooldown|lastTick|nextTick",
    }

    lines = ["# Performance Hotspot Candidates", ""]
    lines += [
        "This is a candidate list, not proof of a performance bug.",
        "Review per-tick, per-frame, world-scan, and network-scan logic carefully.",
        "",
    ]

    for section, pattern in patterns.items():
        matches = grep_files(java_files, pattern)
        lines += [f"## {section}", ""]
        if matches:
            for m in matches:
                clean = m.line.replace("`", "\\`")
                lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
        else:
            lines.append("No matches found.")
        lines.append("")

    return "\n".join(lines)


def risk_hotspots(root: Path, java_files: list[Path]) -> str:
    patterns = {
        "Persistence / codec risks": r"Codec|MapCodec|RecordCodecBuilder|save\(|load\(|read\(|write\(|CompoundTag|ValueInput|ValueOutput|DataComponentType",
        "Networking risks": r"CustomPacketPayload|StreamCodec|ByteBufCodecs|registerPayload|playToServer|playToClient|IPayloadContext",
        "Enum ordinal / ID risks": r"\.ordinal\(|byId|fromId|id\(|getId",
        "Permission / debug risks": r"hasPermissions|permission|debug|admin|operator|op-only|Config\.SERVER",
        "Static mutable candidates": r"public static(?! final)|static\s+(?:Map|HashMap|List|ArrayList|Set|HashSet|Object2|Int2|Long2)",
        "Threading / async candidates": r"new Thread|CompletableFuture|Executor|synchronized|volatile|ConcurrentHashMap",
        "Randomness candidates": r"RandomSource|Math\.random|new Random|ThreadLocalRandom",
        "Attachment / capability candidates": r"AttachmentType|Capability|capability|attach|serialize|deserialize",
    }

    lines = ["# Risk Hotspot Candidates", ""]
    lines += [
        "This is a candidate list, not proof of a bug.",
        "Use it to decide where agents should be cautious.",
        "",
    ]

    for section, pattern in patterns.items():
        matches = grep_files(java_files, pattern)
        lines += [f"## {section}", ""]
        if matches:
            for m in matches:
                clean = m.line.replace("`", "\\`")
                lines.append(f"- `{rel(m.path, root)}` L{m.line_no}: `{clean}`")
        else:
            lines.append("No matches found.")
        lines.append("")

    return "\n".join(lines)


def dependencies_index(root: Path) -> str:
    candidates = [
        "build.gradle",
        "settings.gradle",
        "gradle.properties",
        "gradle/libs.versions.toml",
        "build.gradle.kts",
        "settings.gradle.kts",
    ]

    lines = ["# Dependencies and Build Files", ""]
    found = [root / c for c in candidates if (root / c).exists()]

    if not found:
        lines += ["No standard Gradle build files found.", ""]
        return "\n".join(lines)

    for path in found:
        lines += [f"## `{rel(path, root)}`", ""]
        for line_no, line in iter_lines(path):
            stripped = line.strip()
            if (
                "id " in stripped
                or "implementation" in stripped
                or "compileOnly" in stripped
                or "runtimeOnly" in stripped
                or "modImplementation" in stripped
                or "minecraft" in stripped.lower()
                or "neoforge" in stripped.lower()
                or "version" in stripped.lower()
                or "group" in stripped.lower()
                or "java" in stripped.lower()
            ):
                clean = stripped.replace("`", "\\`")
                lines.append(f"- L{line_no}: `{clean}`")
        lines.append("")

    return "\n".join(lines)


def validation_template() -> str:
    today = _dt.date.today().isoformat()
    return f"""# Validation

Last updated: {today}

This file is a template generated by `scripts/scan_project.py`.

The scan script does not run Gradle and does not claim validation success.
Update this file manually after actually running commands.

## Commands

| Command | Status | Notes |
|---|---:|---|
| `./gradlew compileJava` | not run | |
| `./gradlew runData` | not run | |
| `./gradlew build` | not run | |
| dedicated-server import scan | not run | See command below. |

## Dedicated-server safety command

```bash
rg -n "import net\\.minecraft\\.client|import net\\.neoforged\\.neoforge\\.client|import org\\.mydrugs\\.mydrugs\\.client" src/main/java/org/mydrugs/mydrugs --glob '!client/**'
```

## Latest notes

- Generated scan files exist under `scan/`.
- No compile, datagen, build, or in-game validation is implied by this scan.
"""


def write_simple_lists(root: Path, out: Path, java_files: list[Path]) -> None:
    resource_files = iter_files(root, "src/main/resources")
    generated_resource_files = iter_files(root, "src/generated/resources")

    write(out / "java_files.txt", "\n".join(rel(p, root) for p in java_files) or "No Java files found.")
    write(out / "resource_files.txt", "\n".join(rel(p, root) for p in resource_files) or "No hand-authored resource files found.")
    write(out / "generated_resource_files.txt", "\n".join(rel(p, root) for p in generated_resource_files) or "No generated resource files found.")


def run(root: Path, out_dir: Path, quiet: bool = False) -> int:
    root = root.resolve()
    out = (root / out_dir).resolve() if not out_dir.is_absolute() else out_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)

    java_files = iter_files(root, "src/main/java", suffixes={".java"})
    text_files = iter_text_files(root, "src/main/java", "src/main/resources", "src/generated/resources", "docs")

    if not quiet:
        print(f"Scanning root: {root}")
        print(f"Writing scan files to: {out}")
        print(f"Java files: {len(java_files)}")
        print(f"Text/resource/doc files: {len(text_files)}")

    write(out / "tree.txt", compact_tree(root, ["src/main/java/org/mydrugs/mydrugs", "src/main/resources", "src/generated/resources", "docs", "scripts"]))
    write_simple_lists(root, out, java_files)

    write(out / "packages.md", package_index(root, java_files))
    write(out / "symbols.md", symbol_index(root, java_files))
    write(out / "registries.md", registry_index(root, java_files))
    write(out / "network_payloads.md", network_index(root, java_files))
    write(out / "client_server_violations.md", client_server_scan(root, java_files))
    write(out / "component_literal_report.md", component_literal_report(root, java_files))
    write(out / "todos.md", todos_report(root, text_files))
    write(out / "resources.md", resources_index(root))
    write(out / "generated_vs_authored.md", generated_vs_authored(root))
    write(out / "localization.md", localization_index(root, java_files))
    write(out / "datagen.md", datagen_index(root, java_files))
    write(out / "recipes.md", recipes_index(root, java_files))
    write(out / "tags.md", tags_index(root, java_files))
    write(out / "loot_tables.md", loot_tables_index(root, java_files))
    write(out / "models_and_blockstates.md", models_and_blockstates_index(root))
    write(out / "guide_references.md", guide_references(root))
    write(out / "performance_hotspots.md", performance_hotspots(root, java_files))
    write(out / "risk_hotspots.md", risk_hotspots(root, java_files))
    write(out / "dependencies.md", dependencies_index(root))

    validation_path = out / "validation.md"
    if not validation_path.exists():
        write(validation_path, validation_template())

    if not quiet:
        generated = sorted(p.name for p in out.iterdir() if p.is_file())
        print("\nGenerated scan files:")
        for name in generated:
            print(f"- {out_dir.as_posix()}/{name}")

    return 0


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate factual scan files for the mydrugs codebase.")
    parser.add_argument("--root", default=".", help="Repository root. Default: current directory.")
    parser.add_argument("--out-dir", default="scan", help="Output directory relative to root. Default: scan.")
    parser.add_argument("--quiet", action="store_true", help="Only print errors.")
    return parser.parse_args(argv)


def main(argv: list[str]) -> int:
    args = parse_args(argv)
    root = Path(args.root)
    out_dir = Path(args.out_dir)

    if not root.exists():
        print(f"ERROR: root does not exist: {root}", file=sys.stderr)
        return 2

    try:
        return run(root, out_dir, quiet=args.quiet)
    except KeyboardInterrupt:
        print("Interrupted.", file=sys.stderr)
        return 130
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
