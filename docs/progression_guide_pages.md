# FORMAT REFERENCE (not a page — do not remove this block)
#
# Each page is separated by a line containing only "---"
# Elements per page:
#   # Title         → page title (required, first # in the section)
#   ## Heading      → section heading inside the page
#   Regular text    → body paragraph (consecutive lines merge)
#   > [TIP] text   → green tip callout
#   > [WARN] text  → red warning callout
#   > [GOAL] text  → blue goal callout
#   @title text     → large centered title element
#   @link target|label → clickable link to another page
#   @item ns:id    → renders the item icon + name
#   ***             → thin horizontal separator inside the page
#   ---             → page break (must be alone on its line)
#
# Text is automatically word-wrapped. Keep paragraphs short.
# Run tools/sync_progression_guide.ps1 to regenerate pages.json.

---

# MyDrugs Field Guide

@title MyDrugs Field Guide

This is a quick progression manual. It tells you what to do next, what to save, and where to look when a recipe does not run.

JEI gives exact ingredients. Advancements show direction. Machine GUIs explain blocked recipes. This guide gives the route.

> [GOAL] Follow MAIN QUEST pages first. Read RECOVERY QUEST pages early, not after things go wrong.

@link Quick Index|Open quick index

---

# Quick Index

@title Quick Index

@link Quest Labels|How to read pages
@link Main Route|Full route in one page
@link Knowledge Order|Unlock order
@link Early Crops|Seeds and wild finds
@link Coffee|Chapter 1
@link Tobacco and Recovery|Chapter 2
@link Psy Anvil and Sieve|Chapter 3
@link Cannabis|Chapter 4
@link Fermentation|Chapter 5
@link Hash and Steel|Chapter 6
@link Machine Era|Chapter 7
@link Coca Route|Chapter 8
@link Lab Chemistry|Chapter 9
@link LSD Route|Chapter 10
@link Meth Route|Chapter 11
@link Mushrooms|Chapter 12
@link Psy Mixer|Ritual mixes
@link Psychotrope Energy|Late power
@link Mutations|Endgame body progression
@link Recovery Tools|Survival tools
@link Stuck Checklist|Fix problems fast

---

# Quest Labels

MAIN QUEST advances the knowledge spine.
SUPPORT QUEST makes tools, materials, machines, fluids, or gases.
RECOVERY QUEST keeps addiction, withdrawal, stress, overdose, infection, and bad trips manageable.
RITUAL QUEST means Psy Mixer work.
ENDGAME QUEST means psychotrope energy or mutations.

> [TIP] If you are lost, check your latest knowledge, then follow the next MAIN QUEST.

---

# Main Route

Quest label: OVERVIEW

Seeds -> coffee -> Caffeine Knowledge -> Psy Receptacle -> tobacco -> Nicotinic Knowledge -> Psy Anvil -> iron mesh -> sieve -> cannabis -> Cannabinoid Knowledge -> fermentation -> Fermented Knowledge -> heavy iron -> stomp crafter -> hash -> Steel Plating Knowledge -> machines -> coca -> Stimulant Knowledge -> circuits -> LSD -> Lysergic Knowledge -> advanced circuits -> meth -> Overclocked Knowledge -> mushrooms -> Mycelial Knowledge -> psychotrope energy -> mutations.

> [WARN] Hash comes after Fermented Knowledge because the stomp crafter needs heavy iron.

@link Knowledge Order|Next: knowledge order

---

# Knowledge Order

Quest label: MAIN QUEST

1. Coffee -> Caffeine Knowledge.
2. Processed tobacco -> Nicotinic Knowledge.
3. Processed cannabis -> Cannabinoid Knowledge.
4. Fermented mash -> Fermented Knowledge.
5. Hash -> Steel Plating Knowledge.
6. Cocaine powder -> Stimulant Knowledge.
7. LSD Drop -> Lysergic Knowledge.
8. Meth powder by smoking route -> Overclocked Knowledge.
9. Magic mushroom -> Mycelial Knowledge.

> [WARN] Using something too early may give effects or addiction, but no knowledge.

> [GOAL] Do not rush. Unlock the prerequisite knowledge first.

---

# Early Crops

Quest label: MAIN QUEST + EXPLORATION

Break grass, tall grass, ferns, and large ferns without shears.

@item mydrugs:coffee_seeds
@item mydrugs:tobacco_seeds
@item mydrugs:cannabis_seeds
@item mydrugs:coca_seeds
@item mydrugs:rye_seeds
@item mydrugs:malt_seeds

Grow all six crops. Keep separate rows.

Also collect useful wild finds when exploring.

@item mydrugs:aloe_vera
@item mydrugs:bitter_nut
@item mydrugs:third_eye_petal
@item mydrugs:magic_mushroom

> [GOAL] Start a farm before deep processing.

---

# Coffee

Quest label: MAIN QUEST, Chapter 1

@item mydrugs:coffee_cherries
@item mydrugs:manual_coffee_pulper
@item mydrugs:wet_coffee_bean
@item mydrugs:coffee_bean
@item mydrugs:coffee_powder
@item mydrugs:clay_vat
@item mydrugs:cup
@item mydrugs:coffee_cup
@item mydrugs:psy_receptacle

Pulp cherries -> dry wet beans -> grind beans -> brew powder in heated clay vat with water -> fill cup -> drink.

First coffee gives Caffeine Knowledge and the Psy Receptacle.

> [TIP] A first-use diary entry should point you toward tobacco next.

> [GOAL] Drink coffee and keep the Psy Receptacle.

@link Resin and Tools|Next: tools

---

# Resin and Tools

Quest label: SUPPORT QUEST

@item mydrugs:resin
@item mydrugs:treated_planks
@item mydrugs:drying_rack
@item mydrugs:grinding_bowl
@item mydrugs:grinding_tool
@item mydrugs:portable_grinder

Use an axe repeatedly on strip-able logs to get ordinary resin. Combine resin with planks to make treated planks.

Build drying rack, grinding bowl, and grinding tool. These process most early crops.

> [WARN] Ordinary resin is not cannabis resin.

> [GOAL] Build drying and grinding before tobacco.

---

# Tobacco and Recovery

Quest label: MAIN QUEST + RECOVERY QUEST, Chapter 2

@item mydrugs:tobacco_leaf
@item mydrugs:dried_tobacco_leaf
@item mydrugs:tobacco_handful
@item mydrugs:bang
@item mydrugs:roller
@item mydrugs:cigaret

Dry tobacco leaves -> grind into tobacco handfuls -> smoke directly or roll a cigarette.

Processed tobacco gives Nicotinic Knowledge.

> [WARN] Addiction rates are high. Start recovery habits now, not in late game.

> [GOAL] Unlock Nicotinic Knowledge, then prepare recovery tools.

@link Early Recovery|Recovery now

---

# Early Recovery

Quest label: RECOVERY QUEST

@item mydrugs:personal_diary
@item mydrugs:headphones
@item mydrugs:herbal_tea
@item mydrugs:calming_mixture

Use the diary to read your state. Headphones, herbal tea, and calming mixture help manage stress and withdrawal pressure.

Recovery is not only late-game. Repeated coffee, tobacco, and cannabis use can already matter.

> [TIP] The diary should explain first-time consumption and suggest the next safe step.

> [GOAL] Keep at least one recovery option before repeated use.

---

# Psy Anvil and Sieve

Quest label: MAIN QUEST + SUPPORT QUEST, Chapter 3

@item mydrugs:psy_receptacle
@item mydrugs:psy_anvil
@item mydrugs:stone_hammer
@item mydrugs:iron_mesh
@item mydrugs:sieve

After Caffeine and Nicotinic Knowledge, craft the Psy Anvil.

Use the Psy Anvil to make iron mesh, then craft the sieve.

If the anvil refuses, read its message. It tells you if knowledge is missing.

> [GOAL] Craft iron mesh and build the sieve.

---

# Cannabis

Quest label: MAIN QUEST, Chapter 4

@item mydrugs:cannabis_leaf
@item mydrugs:cured_cannabis_leaf
@item mydrugs:dried_cannabis_leaf
@item mydrugs:cannabis_resin
@item mydrugs:cannabis_powder

Dry cannabis leaves into cured leaves. Sieve cured leaves. Save all cannabis resin. Grind dried cannabis leaf into cannabis powder.

Smoke cannabis powder or roll it to gain Cannabinoid Knowledge after Nicotinic Knowledge.

> [WARN] Sieve before grinding. Resin is needed later for hash.

> [GOAL] Unlock Cannabinoid Knowledge and store every resin piece.

---

# Copper Workshop

Quest label: SUPPORT QUEST

@item mydrugs:copper_plate
@item mydrugs:copper_strapping
@item mydrugs:copper_tube
@item mydrugs:wooden_frame
@item mydrugs:mixing_vat
@item mydrugs:mixing_spatula

Cannabinoid Knowledge unlocks copper shaping on the Psy Anvil.

Use copper parts to build the mixing vat. This starts the fluid era.

> [TIP] Copper is a bottleneck. Do not spend it carelessly.

> [GOAL] Build a mixing vat.

---

# Fermentation

Quest label: MAIN QUEST, Chapter 5

@item mydrugs:rye
@item mydrugs:malt
@item mydrugs:malt_powder
@item mydrugs:wild_yeast_bucket
@item mydrugs:sweet_mash_bucket
@item mydrugs:fermented_mash_bucket
@item mydrugs:glass_bottle

Use the mixing vat to make mash, sweet mash, then fermented mash. JEI gives exact fluid recipes.

Drink fermented mash from a MyDrugs glass bottle after Cannabinoid Knowledge.

> [GOAL] Unlock Fermented Knowledge.

---

# Heavy Iron

Quest label: SUPPORT QUEST

@item mydrugs:heavy_iron
@item mydrugs:heavy_iron_plate
@item mydrugs:mechanical_frame
@item mydrugs:reinforced_casing
@item mydrugs:iron_hammer

Fermented Knowledge unlocks heavy iron on the Psy Anvil.

Heavy iron leads to stronger frames, casings, the stomp crafter, and early machines.

> [GOAL] Craft enough heavy iron for the stomp crafter and machine parts.

---

# Hash and Steel

Quest label: MAIN QUEST, Chapter 6

@item mydrugs:stomp_crafter
@item mydrugs:stomp_plate
@item mydrugs:cannabis_resin
@item mydrugs:hash_brick
@item mydrugs:hash_piece
@item mydrugs:steel_blend
@item mydrugs:steel_ingot
@item mydrugs:steel_plate

Use the stomp crafter to press saved cannabis resin into hash. Split hash bricks into hash pieces. Smoke hash to unlock Steel Plating Knowledge.

Then make steel and craft steel plates on the Psy Anvil.

> [GOAL] Unlock Steel Plating Knowledge and enter the machine era.

---

# Machine Era

Quest label: SUPPORT QUEST, Chapter 7

@item mydrugs:advanced_furnace
@item mydrugs:distiller
@item mydrugs:centrifuge
@item mydrugs:fluid_filterer
@item mydrugs:evaporation_tray
@item mydrugs:advanced_mixing_vat

Build machines as JEI and advancements require them. Machine GUIs show why a recipe is blocked.

Common blocked reasons: missing input, wrong fluid, wrong gas, no heat, no energy, output full, side config, missing knowledge.

> [TIP] Read machine status before assuming a recipe is broken.

> [GOAL] Build advanced furnace, distiller, centrifuge, filterer, tray, and advanced vat.

---

# Common Materials

Quest label: SUPPORT QUEST

@item mydrugs:plant_biomass
@item mydrugs:coal_dust
@item mydrugs:activated_coal
@item mydrugs:raw_rubber
@item mydrugs:rubber
@item mydrugs:thick_glass
@item mydrugs:glass_tube
@item mydrugs:tight_seal
@item mydrugs:fluid_filter
@item mydrugs:refractory_brick

Keep extra biomass, coal dust, rubber, glass tubes, seals, filters, refractory materials, copper tubes, and steel plates.

> [GOAL] Stock support materials before coca and lab chemistry.

---

# World Materials

Quest label: EXPLORATION QUEST

@item mydrugs:aluminium_ore
@item mydrugs:platinum_ore
@item mydrugs:sulfur_ore
@item mydrugs:sulfur_powder
@item mydrugs:salt_powder
@item mydrugs:petroleum_bucket

Mine sulfur, aluminium, and platinum. Find salt in ocean terrain. Mark desert petroleum.

Platinum, aluminium, sulfur, salt, and petroleum support late chemistry.

> [GOAL] Gather world materials before deep lab routes.

---

# Alcohol Distillation

Quest label: SUPPORT QUEST

@item mydrugs:fermented_mash_bucket
@item mydrugs:low_wines_bucket
@item mydrugs:raw_alcohol_bucket
@item mydrugs:ethanol_bucket
@item mydrugs:absolute_ethanol_bucket

Distill fermented mash -> low wines -> raw alcohol -> ethanol. Mix ethanol with salt powder to make absolute ethanol.

Absolute ethanol is needed for coca and lab chemistry.

> [GOAL] Produce absolute ethanol.

---

# Coca Route

Quest label: MAIN QUEST, Chapter 8

@item mydrugs:coca_leaf
@item mydrugs:dried_coca_leaf
@item mydrugs:coca_paste
@item mydrugs:murky_extract_bucket
@item mydrugs:filtered_extract_bucket
@item mydrugs:cocaine_plate
@item mydrugs:cocaine_powder

Dry coca -> grind paste -> mix with absolute ethanol -> centrifuge -> evaporate -> grind plate into powder.

Consume cocaine powder after Fermented Knowledge to unlock Stimulant Knowledge.

> [WARN] Cocaine and crack raise addiction, bad trip, and overdose pressure quickly.

> [GOAL] Unlock Stimulant Knowledge.

---

# Cocaine and Crack

Quest label: SIDE QUEST + RISK ROUTE

@item mydrugs:cocaine_powder
@item mydrugs:cupboard_piece
@item mydrugs:crack_plate
@item mydrugs:crack_shard

Cocaine can be powder, rail, or rolling ingredient. To make a rail, place powder pile, shape with cardboard, then use the rail.

Crack is a stronger smoking branch. Follow JEI for advanced vat and tray steps.

> [WARN] Prepare recovery before repeated use.

---

# Stimulant Tech

Quest label: SUPPORT QUEST

@item mydrugs:insulated_wire
@item mydrugs:control_circuit
@item mydrugs:electric_motor
@item mydrugs:heating_coil
@item mydrugs:condenser_coil
@item mydrugs:electrode_pair

Stimulant Knowledge unlocks repeatable insulated wire. Use it for control circuits and powered machine parts.

> [GOAL] Craft control circuits and expand automation.

---

# Pipes and Transfers

Quest label: UTILITY QUEST

@item mydrugs:basic_item_pipe
@item mydrugs:basic_fluid_pipe
@item mydrugs:basic_gas_pipe
@item mydrugs:pipe_wrench
@item mydrugs:pipe_filter_upgrade
@item mydrugs:machine_transfer_upgrade

Use the correct pipe type. Configure machine sides with upgrades. Use filters when only one resource should move.

> [TIP] If a line stops, check output space, tank type, gas tank, pipe direction, side config, filters, and energy.

---

# Lab Chemistry

Quest label: SUPPORT QUEST, Chapter 9

@item mydrugs:gas_tank
@item mydrugs:gas_pump
@item mydrugs:fluid_pump
@item mydrugs:chemical_reactor
@item mydrugs:gasifier
@item mydrugs:electrolyzer
@item mydrugs:advanced_mixing_vat

Late recipes use fluids and gases. Items, fluids, and gases are separate inputs.

Build gas storage, pumps, chemical reactor, gasifier, electrolyzer, and advanced mixing vat.

> [GOAL] Prepare gas and chemical handling before LSD and meth.

---

# Acids and Reagents

Quest label: SUPPORT QUEST

@item mydrugs:brine_bucket
@item mydrugs:hydrochloric_acid_bucket
@item mydrugs:sulfuric_acid_bucket
@item mydrugs:acylating_agent_bucket
@item mydrugs:amino_acid_bucket
@item mydrugs:tryptophan
@item mydrugs:diethylamine_bucket

Use JEI to produce acids and reagents from brine, gases, plant biomass, malt, water, ethanol, and ammoniac.

> [TIP] If an LSD recipe is blocked, the missing input is often a gas or acid.

> [GOAL] Stock hydrochloric acid, sulfuric acid, acylating agent, tryptophan, and diethylamine.

---

# LSD Route

Quest label: MAIN QUEST, Chapter 10

@item mydrugs:growth_chamber
@item mydrugs:biochemical_reactor
@item mydrugs:fungal_culture
@item mydrugs:infected_rye
@item mydrugs:ergot
@item mydrugs:ergotamine
@item mydrugs:lysergic_acid_bucket
@item mydrugs:lsd_bucket
@item mydrugs:lsd_drop

Grow fungal culture, infect rye, harvest ergot, process through biochemical and chemical routes, then make LSD fluid.

Drop cardboard pieces and use a bottle containing LSD to make LSD Drops.

Consume an LSD Drop after Stimulant Knowledge to unlock Lysergic Knowledge.

> [GOAL] Unlock Lysergic Knowledge.

---

# Advanced Circuits

Quest label: SUPPORT QUEST

@item mydrugs:advanced_control_circuit
@item mydrugs:reaction_core
@item mydrugs:catalyst_bed
@item mydrugs:packed_column
@item mydrugs:valve
@item mydrugs:membrane
@item mydrugs:injector_nozzle

Lysergic Knowledge unlocks advanced control circuits and high-tier machine parts.

> [GOAL] Build advanced circuits before petroleum and meth routes.

---

# Petroleum Route

Quest label: SUPPORT QUEST

@item mydrugs:petroleum_bucket
@item mydrugs:naphtha_bucket
@item mydrugs:coal_tar_bucket
@item mydrugs:reformate_bucket
@item mydrugs:btx_mix_bucket
@item mydrugs:benzene_bucket
@item mydrugs:toluene_bucket
@item mydrugs:xylene_bucket
@item mydrugs:steam_cracker
@item mydrugs:catalytic_reformer
@item mydrugs:aromatic_extractor
@item mydrugs:btx_fractionation_tower

Build the cracker, reformer, extractor, and BTX tower before batch-producing meth precursors.

> [GOAL] Produce benzene and propylene for late meth chemistry.

---

# Meth Route

Quest label: MAIN QUEST, Chapter 11

@item mydrugs:methanol_bucket
@item mydrugs:methylamine_bucket
@item mydrugs:acetone_bucket
@item mydrugs:chloroacetone_bucket
@item mydrugs:phenylacetone_bucket
@item mydrugs:methamphetamine_bucket
@item mydrugs:meth_shard
@item mydrugs:meth_powder

Use high-tier machines, gases, reactors, aromatics, and evaporation. Grind meth shards into meth powder.

Smoke meth powder after Lysergic Knowledge to unlock Overclocked Knowledge.

> [WARN] This is a late route, not a shortcut.

> [GOAL] Unlock Overclocked Knowledge.

---

# Mushrooms

Quest label: MAIN QUEST + EXPLORATION, Chapter 12

@item mydrugs:magic_mushroom
@item mydrugs:magic_mushroom_powder
@item mydrugs:mycelial_resonator
@item mydrugs:vanilla_biome_finder
@item mydrugs:shroom_harvester

Magic mushrooms can be found earlier, but Mycelial Knowledge only unlocks after Overclocked Knowledge.

After Overclocked Knowledge, consume a magic mushroom to unlock Mycelial Knowledge. Use mushroom powder for late recipes.

> [GOAL] Find psychedelic terrain and unlock Mycelial Knowledge.

---

# Psy Mixer

Quest label: RITUAL QUEST

@item mydrugs:painted_clay_bowl
@item mydrugs:psychotropic_pigment
@item mydrugs:ritual_resin
@item mydrugs:ritual_threads
@item mydrugs:unstable_residue

Psy Mixer recipes use a base drug, material, vessel, knowledge, and sometimes drug history. Failure may create unstable residue.

Use JEI for exact recipe requirements.

> [TIP] Psy Mixer mixes are not required for the main spine, but they make work, rituals, exploration, and risk management stronger.

---

# Better Mixes

Quest label: RITUAL QUEST, safer support

@item mydrugs:brightened_cannabis_powder
@item mydrugs:soothing_tobacco_blend
@item mydrugs:defiant_spirit_bottle

Useful early ritual themes:
Coffee + sugar/redstone/cocoa/bitter nut -> work speed or mining speed.
Tobacco + aloe/quartz/copper -> precision and tremor reduction.
Weed/hash + moss/calming spores/ghast tear -> stress relief and ritual stability.
Alcohol + ghast tear/broken courage -> stress or damage resistance.

> [GOAL] Try tobacco, coffee, weed, and hash mixes before harder stimulant rituals.

---

# Speed Burst Vision

Quest label: RITUAL QUEST, stronger effects

Cocaine + redstone/blaze powder/rabbit foot/charged sinew -> movement, manual speed, adrenaline, dash.
Crack + gunpowder/echo shard/fractured impulse -> burst windows and dash power.
Meth + diamond/netherite scrap/charged core -> mining speed, work speed, adrenaline, but more tremor/input risk.
LSD + lapis/diamond/ender pearl/third eye petal -> ore aura, fortune, multiblock vision, ritual focus.
Mushrooms + amethyst/glow berries/dreamcap spores -> ore aura, gamma, ritual focus, bad-trip resistance.

> [WARN] Strong mixes are not safe mixes. Watch stress, symptoms, addiction, and overdose pressure.

---

# Psychotrope Energy

Quest label: ENDGAME QUEST

@item mydrugs:psychotrope_lens
@item mydrugs:psychotrope_component
@item mydrugs:psychotrope_core
@item mydrugs:energy_upgrade
@item mydrugs:automation_upgrade

Craft lens -> components -> core. The core converts drug value into psychotrope energy.

Use energy for late machines and mutation work.

> [WARN] Feeding strong drugs into energy is a resource trade. Do it only when production can replace them.

> [GOAL] Build stable psychotrope energy before mutations.

---

# Bottles and Syringes

Quest label: UTILITY QUEST + MUTATION PREP

@item mydrugs:glass_bottle
@item mydrugs:syringe
@item mydrugs:blood_bucket
@item mydrugs:autoclave
@item mydrugs:mutagenic_blood_vial

Bottles drink valid mod fluids and create LSD Drops.

Syringes draw blood and inject mutation payloads. Dirty syringes can cause infection. Autoclave empty dirty syringes before blood or mutation work.

> [WARN] Dirty mutation injection rejects the payload and starts infection.

---

# Mutations

Quest label: ENDGAME QUEST

@item mydrugs:adn_scraper
@item mydrugs:adn_scrap
@item mydrugs:adn_gene
@item mydrugs:mutation_vector
@item mydrugs:mutagenic_blood_vial
@item mydrugs:syringe

Route: scrape ADN -> extract genes -> combine genes -> incubate vector -> infuse with blood -> load sterile syringe -> inject -> assimilate over time.

Mutations improve drug-system risks and strengths, but dirty injections, infection, rejection, and instability can punish bad preparation.

> [GOAL] Use mutations to solve the problems your playstyle creates.

---

# Mutation Machines

Quest label: ENDGAME QUEST

@item mydrugs:gene_extractor
@item mydrugs:crispr_cas9_combinator
@item mydrugs:bacterial_incubator
@item mydrugs:hemogenic_infuser
@item mydrugs:autoclave

Gene Extractor: ADN Scrap -> single-stat genes.
CRISPR-CAS9: combine compatible genes from different sources.
Bacterial Incubator: gene -> mutation vector.
Hemogenic Infuser: vector + blood -> mutagenic blood vial.
Autoclave: sterilize dirty empty syringes.

> [WARN] Complex genes are stronger but riskier.

---

# Mutation Builds

Quest label: ENDGAME REFERENCE

Clear Mind: Mental Strength, Visual Accuracy, Withdrawal Resilience.
Durable Body: Health Stability, Metabolic Control, Infection Resistance.
Ritualist: Ritual Neural Sync, Genetic Stability, Visual Accuracy.
Safe Chemist: Infection Resistance, Health Stability, Genetic Stability.
Overclocker: Metabolic Control, Pleasure Sensitivity, Addiction Resistance.

> [TIP] Balanced builds are safer than one huge stat.

---

# Recovery Tools

Quest label: RECOVERY QUEST

@item mydrugs:personal_diary
@item mydrugs:headphones
@item mydrugs:herbal_tea
@item mydrugs:calming_mixture
@item mydrugs:sleeping_aid
@item mydrugs:overdose_antidote
@item mydrugs:therapist_desk
@item mydrugs:recovery_anchor

Diary reads your condition. Headphones support you while carried. Tea and calming mixture reduce stress or withdrawal. Sleeping aid helps blocked sleep. Antidote reduces overdose danger. Therapist desk and recovery anchor create stronger support.

> [GOAL] Build a recovery corner near your lab before stimulant, crack, meth, infection, or mutation work.

---

# Side Content

Quest label: SIDE QUEST

@item mydrugs:space_apple
@item mydrugs:space_bread
@item mydrugs:thunder_bottle
@item mydrugs:lightning_bottle
@item mydrugs:fractured_impulse
@item mydrugs:broken_courage
@item mydrugs:charged_core
@item mydrugs:charged_sinew

Space food and special ritual materials are useful side content. Keep samples and check JEI before mass crafting.

> [TIP] If it does not clearly advance your last knowledge gate, treat it as side content.

---

# Stuck Checklist

Quest label: HELP

1. Check your latest knowledge.
2. Check the advancement parent.
3. Check JEI category.
4. Read the machine or Psy Anvil status text.
5. Check item slots, fluids, gases, heat, energy, output space, side config, pipes, and filters.
6. Check correct consumption form.

Common forms: fresh leaves usually do not unlock knowledge. Meth shards must become meth powder. LSD fluid must become LSD Drops. Coca can be powder or rail.

> [GOAL] When stuck, follow the last knowledge you unlocked.

---

# Progression Summary

Quick route:
Grass -> crops -> coffee -> Psy Receptacle -> tobacco -> Psy Anvil -> iron mesh -> sieve -> cannabis -> copper -> mixing vat -> fermented mash -> heavy iron -> stomp crafter -> hash -> steel -> machines -> absolute ethanol -> coca -> circuits -> gases/acids -> LSD -> advanced circuits -> petroleum -> meth -> mushrooms -> psychotrope energy -> mutations.

Parallel priorities:
Recovery early. JEI for ingredients. Machine status for problems. Psy Mixer for stronger mixes. Pipes for automation. Diary for your condition.

> [GOAL] Finish the knowledge spine, then use psychotrope energy, rituals, and mutations as the endgame sandbox.
