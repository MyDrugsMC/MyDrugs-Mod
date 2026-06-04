<#
.SYNOPSIS
    Parses docs/progression_guide_pages.md and generates
    src/main/resources/assets/mydrugs/guide/pages.json.

    Also removes the now-unused book.mydrugs.progression_guide.page.* entries
    from the lang file so the JSON stays clean.

.DESCRIPTION
    Markdown element markers (within a page block):
        # Title        - page title (first # per page, required)
        ## Heading     - section heading inside the page
        Regular text   - body paragraph (consecutive lines merge into one block)
        > [TIP] text  - green tip callout
        > [WARN] text - red warning callout
        > [GOAL] text - blue goal callout
        @item ns:id   - item icon element
        @link target|label - clickable page link
        @title text   - large title element
        ***            - thin separator inside the page
        ---            - page break (must be alone on its line)

    Lines appearing before the FIRST --- are treated as file header/comments
    and are ignored entirely.

.PARAMETER Root
    Repository root. Defaults to the parent of this script's directory.

.PARAMETER Source
    Relative path to the markdown source. Default: docs\progression_guide_pages.md

.PARAMETER DryRun
    Print the generated JSON to stdout instead of writing files.
#>
param(
    [string]$Root   = (Resolve-Path "$PSScriptRoot\..").Path,
    [string]$Source = "docs\progression_guide_pages.md",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

# ─── Paths ───────────────────────────────────────────────────────────────────

$sourcePath  = Join-Path $Root $Source
$jsonOutPath = Join-Path $Root "src\main\resources\assets\mydrugs\guide\pages.json"
$langPath    = Join-Path $Root "src\main\resources\assets\mydrugs\lang\en_us.json"

# ─── Helper: write UTF-8 without BOM ─────────────────────────────────────────

function Write-Utf8NoBom([string]$path, [string]$content) {
    $enc = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($path, $content, $enc)
}

# ─── Parse markdown into a list of page hashtables ───────────────────────────

function ConvertFrom-GuideMarkdown([string]$path) {
    if (-not (Test-Path $path)) { throw "Guide source not found: $path" }
    $rawLines = Get-Content -LiteralPath $path -Encoding UTF8

    # --- working state (plain variables, no nested functions) ---
    $pages      = [System.Collections.Generic.List[hashtable]]::new()
    $elements   = [System.Collections.Generic.List[hashtable]]::new()
    $textLines  = [System.Collections.Generic.List[string]]::new()
    $pageTitle  = ""
    $pastHeader = $false

    foreach ($line in $rawLines) {
        # Everything before the first --- is the format comment block; skip it.
        if (-not $pastHeader) {
            if ($line -match '^\s*---\s*$') { $pastHeader = $true }
            continue
        }

        # ── Page break ────────────────────────────────────────────────────────
        if ($line -match '^\s*---\s*$') {
            # Flush accumulated text
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            # Flush current page — @($elements) copies the array contents
            if ($pageTitle -ne "" -or $elements.Count -gt 0) {
                $pages.Add(@{
                    title    = $pageTitle
                    elements = @($elements)
                })
            }
            $elements.Clear()
            $pageTitle = ""
            continue
        }

        # ── Page title (# Title) ──────────────────────────────────────────────
        if ($line -match '^#\s+(.+)$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            if ($pageTitle -eq "") {
                $pageTitle = $Matches[1].Trim()
            } else {
                # second # in same page → treat as heading
                $elements.Add(@{ type = "heading"; text = $Matches[1].Trim() })
            }
            continue
        }

        # ── Section heading (## Heading) ──────────────────────────────────────
        if ($line -match '^##\s+(.+)$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            $elements.Add(@{ type = "heading"; text = $Matches[1].Trim() })
            continue
        }

        # ── Callout: TIP ──────────────────────────────────────────────────────
        if ($line -match '^>\s*\[TIP\]\s*(.*)$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            $elements.Add(@{ type = "tip"; text = $Matches[1].Trim() })
            continue
        }

        # ── Callout: WARN ─────────────────────────────────────────────────────
        if ($line -match '^>\s*\[WARN\]\s*(.*)$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            $elements.Add(@{ type = "warning"; text = $Matches[1].Trim() })
            continue
        }

        # ── Callout: GOAL ─────────────────────────────────────────────────────
        if ($line -match '^>\s*\[GOAL\]\s*(.*)$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            $elements.Add(@{ type = "goal"; text = $Matches[1].Trim() })
            continue
        }

        # ── Item icon (@item ns:id) ───────────────────────────────────────────
        if ($line -match '^@link\s+(.+)$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            $raw = $Matches[1].Trim()
            $parts = $raw.Split('|', 2)
            $target = $parts[0].Trim()
            $label = if ($parts.Count -gt 1) { $parts[1].Trim() } else { $target }
            $elements.Add(@{ type = "link"; text = $label; target = $target })
            continue
        }

        if ($line -match '^@title\s+(.+)$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            $elements.Add(@{ type = "title"; text = $Matches[1].Trim() })
            continue
        }

        if ($line -match '^@item\s+(\S+)') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            $elements.Add(@{ type = "item"; text = $Matches[1].Trim() })
            continue
        }

        # ── Inline separator (***) ────────────────────────────────────────────
        if ($line -match '^\*\*\*\s*$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            $elements.Add(@{ type = "separator"; text = "" })
            continue
        }

        # ── Empty line → flush accumulated text paragraph ─────────────────────
        if ($line -match '^\s*$') {
            if ($textLines.Count -gt 0) {
                $para = ($textLines -join " ").Trim()
                if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
                $textLines.Clear()
            }
            continue
        }

        # ── Body text ─────────────────────────────────────────────────────────
        $textLines.Add($line.Trim())
    }

    # Flush final page
    if ($textLines.Count -gt 0) {
        $para = ($textLines -join " ").Trim()
        if ($para -ne "") { $elements.Add(@{ type = "text"; text = $para }) }
    }
    if ($pageTitle -ne "" -or $elements.Count -gt 0) {
        $pages.Add(@{
            title    = $pageTitle
            elements = @($elements)
        })
    }

    return $pages
}

# ─── Serialise to JSON ────────────────────────────────────────────────────────

function ConvertTo-GuideJson([System.Collections.Generic.List[hashtable]]$pages) {
    $sb = [System.Text.StringBuilder]::new()
    $null = $sb.AppendLine("{")
    $null = $sb.AppendLine('  "pages": [')

    for ($pi = 0; $pi -lt $pages.Count; $pi++) {
        $page     = $pages[$pi]
        $isLast   = ($pi -eq $pages.Count - 1)
        $titleJ   = $page.title | ConvertTo-Json -Compress

        $null = $sb.AppendLine("  {")
        $null = $sb.AppendLine("    ""title"": $titleJ,")
        $null = $sb.AppendLine("    ""elements"": [")

        $elems = $page.elements
        for ($ei = 0; $ei -lt $elems.Count; $ei++) {
            $el       = $elems[$ei]
            $isLastEl = ($ei -eq $elems.Count - 1)
            $typeJ    = $el.type | ConvertTo-Json -Compress
            $textJ    = $el.text | ConvertTo-Json -Compress
            $comma    = if ($isLastEl) { "" } else { "," }
            if ($el.ContainsKey("target")) {
                $targetJ = $el.target | ConvertTo-Json -Compress
                $null = $sb.AppendLine("      { ""type"": $typeJ, ""text"": $textJ, ""target"": $targetJ }$comma")
            } else {
                $null = $sb.AppendLine("      { ""type"": $typeJ, ""text"": $textJ }$comma")
            }
        }

        $null = $sb.AppendLine("    ]")
        $pageComma = if ($isLast) { "" } else { "," }
        $null = $sb.AppendLine("  }$pageComma")
    }

    $null = $sb.AppendLine("  ]")
    $null = $sb.Append("}")
    return $sb.ToString()
}

# ─── Clean old lang entries ───────────────────────────────────────────────────

function Remove-OldLangPageEntries([string]$langPath) {
    if (-not (Test-Path $langPath)) { return }

    $raw     = Get-Content -LiteralPath $langPath -Raw -Encoding UTF8
    $pattern = '(?m)[ \t]*"book\.mydrugs\.progression_guide\.page\.\d+"[ \t]*:[ \t]*"(?:\\.|[^"\\])*"[ \t]*,?[ \t]*\r?\n?'
    $cleaned = [regex]::Replace($raw, $pattern, "")

    if ($cleaned -ne $raw) {
        Write-Utf8NoBom $langPath $cleaned
        Write-Host "  Removed old book page entries from en_us.json" -ForegroundColor DarkGray
    }
}

# ─── Main ─────────────────────────────────────────────────────────────────────

Write-Host "Reading: $sourcePath" -ForegroundColor Cyan

$pages = ConvertFrom-GuideMarkdown $sourcePath

if ($pages.Count -eq 0) {
    throw "No pages parsed. Check that --- separators are present in the markdown."
}

# Warn about pages with no elements (likely a parser issue)
for ($i = 0; $i -lt $pages.Count; $i++) {
    if ($pages[$i].elements.Count -eq 0) {
        Write-Warning "Page $($i + 1) '$($pages[$i].title)' has no elements."
    }
}

Write-Host "Parsed $($pages.Count) pages." -ForegroundColor Green

$json = ConvertTo-GuideJson $pages

if ($DryRun) {
    Write-Host "`n── Generated JSON (DryRun) ──────────────────────────────────" -ForegroundColor Yellow
    Write-Host $json
    exit 0
}

# Ensure output directory exists
$outDir = Split-Path $jsonOutPath -Parent
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

Write-Utf8NoBom $jsonOutPath $json
Write-Host "Written: $jsonOutPath" -ForegroundColor Green

Remove-OldLangPageEntries $langPath

Write-Host ""
Write-Host "Done. Rebuild the mod to apply changes." -ForegroundColor Green
Write-Host ""
Write-Host "Usage:" -ForegroundColor DarkGray
Write-Host "  .\tools\sync_progression_guide.ps1            # normal sync" -ForegroundColor DarkGray
Write-Host "  .\tools\sync_progression_guide.ps1 -DryRun   # preview without writing" -ForegroundColor DarkGray
