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

Welcome. This book explains the survival progression currently available in MyDrugs: crops, hand processing, knowledge gates, machines, laboratory chemistry, rituals, psychotrope energy, mutations, addiction, and recovery.

Keep JEI and the advancement tree open beside this guide. JEI is still the source for exact ingredient counts, fluid amounts, gas amounts, heat, time, and machine category. This guide explains the order and the gameplay purpose of each step.

> [TIP] Right-click the guide at any time to reopen it. The guide item is not consumed.

> [GOAL] Start with coffee, then tobacco, then follow the knowledge chain through the rest of the mod.

@link Table of Contents|Open the table of contents

---

# Table of Contents

@title Table of Contents

@link The Current Spine|Main progression spine
@link Knowledge Gates and Consumption Order|Knowledge gates
@link Seeds and Wild Finds|Seeds and wild plants
@link Coffee First|Coffee and Caffeine Knowledge
@link Tobacco to Nicotinic Knowledge|Tobacco and Nicotinic Knowledge
@link Cannabis Preparation|Cannabis and Cannabinoid Knowledge
@link Fermentation Setup|Fermentation and alcohol
@link Stomp Crafter and Hash|Hash and Steel Plating Knowledge
@link Coca to Stimulant Knowledge|Coca and Stimulant Knowledge
@link Lysergic Route|LSD and Lysergic Knowledge
@link Overclocked Route|Meth and Overclocked Knowledge
@link Mushrooms and Mycelial Knowledge|Mushrooms and Mycelial Knowledge
@link Psy Mixer Rituals|Psy Mixer rituals
@link Psychotrope Research|Psychotrope research
@link Mutation Overview|Mutations
@link Recovery and Addiction|Recovery and addiction
@link When You Are Stuck|Troubleshooting

---

# The Current Spine

The main route is:

Seeds and wild materials -> coffee -> Caffeine Knowledge -> Psy Receptacle -> resin from logs -> treated planks -> drying and grinding -> tobacco -> Nicotinic Knowledge -> Psy Anvil -> iron mesh and sieve -> cannabis -> Cannabinoid Knowledge -> copper shaping -> mixing vat -> fermented mash -> Fermented Knowledge -> heavy iron -> stomp crafter -> hash -> Steel Plating Knowledge -> steel plates and machines -> coca -> Stimulant Knowledge -> insulated wire and circuits -> lysergic route -> Lysergic Knowledge -> advanced circuits -> meth route -> Overclocked Knowledge -> mushrooms -> Mycelial Knowledge -> psychotrope research -> mutation machines.

Parallel systems include recovery, pipes, gases, acids, petroleum chemistry, Psy Mixer rituals, cocaine rails, crack, blood syringes, biome exploration, psychotrope energy, and mutations.

> [WARN] Older notes may say hash comes before heavy iron. In the current recipes, the stomp crafter needs heavy iron, so hash comes after Fermented Knowledge.

> [TIP] If a recipe is visible but refuses to run, check knowledge first. Many locked recipes are visible in JEI before you are allowed to complete them.

@link Knowledge Gates and Consumption Order|Next: knowledge gates

---

# Knowledge Gates and Consumption Order

Knowledge is the backbone of progression. Most gates are unlocked by consuming a processed drug after its prerequisite knowledge is already active.

Coffee grants Caffeine Knowledge with no prerequisite. Tobacco grants Nicotinic Knowledge. Cannabis grants Cannabinoid Knowledge after Nicotinic Knowledge. Fermented mash grants Fermented Knowledge after Cannabinoid Knowledge. Cocaine grants Stimulant Knowledge after Fermented Knowledge. LSD grants Lysergic Knowledge after Stimulant Knowledge. Meth grants Overclocked Knowledge after Lysergic Knowledge. Magic mushrooms grant Mycelial Knowledge after Overclocked Knowledge.

Hash is special. Smoking hash after the cannabis route unlocks Steel Plating Knowledge, which is required for steel plates.

> [WARN] Eating or smoking something too early can still give drug effects, addiction, bad trips, or overdose pressure, but it may not unlock the next knowledge gate.

> [GOAL] Follow the drug order instead of trying to rush the chemical routes.

---

# Seeds and Wild Finds

Break short grass, tall grass, ferns, and large ferns without shears to find the main crop seeds.

@item mydrugs:tobacco_seeds
@item mydrugs:cannabis_seeds
@item mydrugs:coca_seeds
@item mydrugs:rye_seeds
@item mydrugs:malt_seeds
@item mydrugs:coffee_seeds

Plant them on farmland like vanilla crops. Keep separate rows for each crop because later routes need steady supplies, not one-off harvests.

Coffee crops give coffee cherries. Tobacco gives tobacco leaves. Cannabis gives cannabis leaves. Coca gives coca leaves. Rye and malt feed the alcohol and lab branches.

@item mydrugs:coffee_cherries
@item mydrugs:tobacco_leaf
@item mydrugs:cannabis_leaf
@item mydrugs:coca_leaf
@item mydrugs:rye
@item mydrugs:malt

Opium poppy seeds can be crafted from a vanilla poppy and planted, but the current main progression does not use an opium processing route.

Wild materials also matter. Aloe vera appears in overworld patches. Bitter nut bushes appear in forest and jungle-type biomes. Third eye petals appear in mountain-type biomes. Magic mushrooms appear in psychedelic mushroom terrain.

@item mydrugs:aloe_vera
@item mydrugs:bitter_nut
@item mydrugs:third_eye_petal
@item mydrugs:magic_mushroom

> [GOAL] Grow all six main crops, then collect aloe, bitter nuts, third eye petals, and mushrooms when you explore.

---

# Coffee First

Coffee is the first discovery branch. It teaches Caffeine Knowledge, and the first caffeine imprint awards the Psy Receptacle.

@item mydrugs:coffee_cherries
@item mydrugs:manual_coffee_pulper
@item mydrugs:wet_coffee_bean
@item mydrugs:coffee_bean
@item mydrugs:coffee_powder
@item mydrugs:clay_vat
@item mydrugs:cup
@item mydrugs:coffee_cup
@item mydrugs:psy_receptacle

Pulp coffee cherries in the manual coffee pulper to get wet coffee beans and plant biomass. Dry the wet beans, then grind coffee beans into coffee powder.

The reliable early route is the heated clay vat. Put a lit campfire, soul campfire, fire, or soul fire under a clay vat. Add water, then use coffee powder on the heated vat. Each powder brews water into coffee. Craft a cup from brick, then right-click the coffee-filled vat with the cup to make a coffee cup.

The mixing vat route also works later: coffee powder plus water in a heated mixing vat produces coffee fluid.

> [TIP] Caffeine Knowledge awards the Psy Receptacle. Nicotinic Knowledge still matters, but it no longer gives the receptacle.

> [GOAL] Brew coffee, drink a coffee cup, unlock Caffeine Knowledge, and keep the Psy Receptacle.

@link Resin and Treated Planks|Next: resin and treated planks

---

# Resin and Treated Planks

The first processing block depends on treated planks. To make them, gather ordinary resin by repeatedly using an axe on a strip-able log.

@item mydrugs:resin
@item mydrugs:treated_planks

Each axe step drops resin, then the log finishes stripping after enough hits. Combine resin with planks to make treated planks.

> [TIP] This resin is not cannabis resin. Cannabis resin is a later sieve output used for hash.

> [GOAL] Make treated planks before trying to build the drying rack.

---

# Early Processing Tools

Build the drying rack, grinding bowl, and grinding tool. These are the first reliable crop-processing stations.

@item mydrugs:drying_rack
@item mydrugs:grinding_bowl
@item mydrugs:grinding_tool
@item mydrugs:portable_grinder

The drying rack converts fresh or wet materials into dried forms. The grinding bowl plus grinding tool turns dried materials into powders, handfuls, paste, or other intermediates.

The portable grinder is useful later when you want mobile grinding, but the bowl and tool are enough for the first gates.

> [WARN] The sieve is not a first-minute station. It needs iron mesh, and iron mesh needs Nicotinic Knowledge through the Psy Anvil.

> [GOAL] Build a drying rack and grinding bowl, then dry your first tobacco leaves.

---

# Tobacco to Nicotinic Knowledge

Tobacco is the first intended smoking gate.

@item mydrugs:tobacco_leaf
@item mydrugs:dried_tobacco_leaf
@item mydrugs:tobacco_handful

Dry tobacco leaves on the drying rack. Grind dried tobacco leaves in the grinding bowl to obtain tobacco handfuls. Smoke tobacco handfuls directly through the bang, or roll three tobacco ingredients into a cigarette.

Consuming processed tobacco grants Nicotinic Knowledge.

> [WARN] Fresh tobacco leaf does not grant the knowledge. Process it first.

> [GOAL] Consume processed tobacco and unlock Nicotinic Knowledge.

@link Psy Receptacle and Psy Anvil|Next: Psy Anvil

---

# Rolling and Smoking

The roller combines paper, a cigarette filter, and three rolling ingredients. Three tobacco ingredients make a cigarette. Mixed or non-tobacco smoking ingredients make a joint.

@item mydrugs:roller
@item mydrugs:cigaret_filter
@item mydrugs:cigaret
@item mydrugs:joint

The bang can hold a smoking item and consume it through the smoking route.

@item mydrugs:bang

Tobacco handfuls, cannabis powder, hash pieces, cocaine powder, crack shards, and some mixed drug outputs can be rolling ingredients.

> [TIP] Shift-right-click the bang to manage its contents. Use it normally to smoke the loaded item.

> [WARN] The roller output depends on all three ingredients inside the roller, not only on the last item inserted.

---

# Psy Receptacle and Psy Anvil

After Caffeine Knowledge gives you the Psy Receptacle and tobacco gives Nicotinic Knowledge, craft the Psy Anvil.

@item mydrugs:psy_receptacle
@item mydrugs:psy_anvil
@item mydrugs:stone_hammer
@item mydrugs:iron_hammer
@item mydrugs:steel_hammer
@item mydrugs:psy_blueprint

The Psy Anvil handles knowledge-gated recipes. Place the required ingredients, then activate the anvil with the proper hammer or tool as shown by JEI.

Early Psy Anvil gates are iron mesh from Nicotinic Knowledge, copper plates from Cannabinoid Knowledge, heavy iron from Fermented Knowledge, steel plates from Steel Plating Knowledge, insulated wire from Stimulant Knowledge, advanced control circuits from Lysergic Knowledge, and the mycelial resonator from Overclocked Knowledge.

## If the anvil refuses

Check the ingredient list, active knowledge, and tool requirement. Recipes can be visible before you are allowed to complete them.

> [GOAL] Craft the Psy Anvil and use it for iron mesh.

---

# Iron Mesh and the Sieve

The first important Psy Anvil product is iron mesh.

@item mydrugs:iron_mesh
@item mydrugs:sieve

Iron mesh requires Nicotinic Knowledge. Once crafted, use it with copper, sticks, and a wooden frame to make the sieve.

The sieve is essential for the cannabis route because it separates cured cannabis into dried cannabis leaf and cannabis resin.

> [TIP] Do not spend all your early copper. The sieve, copper shaping, trays, tubes, vats, and machines all compete for it.

> [GOAL] Craft iron mesh, then build the sieve.

---

# Cannabis Preparation

Cannabis is the second knowledge route, but it has more steps than tobacco.

@item mydrugs:cannabis_leaf
@item mydrugs:cured_cannabis_leaf
@item mydrugs:dried_cannabis_leaf
@item mydrugs:cannabis_powder

Dry cannabis leaves into cured cannabis leaves. Run cured cannabis through the sieve. The main output is dried cannabis leaf, with cannabis resin as a bonus output. Grind dried cannabis leaf into cannabis powder.

Put cannabis powder in the bang or roll it into a smokable item to gain Cannabinoid Knowledge after Nicotinic Knowledge is active.

> [WARN] Sieve cured cannabis before grinding. Grinding the wrong stage skips the resin route.

> [GOAL] Unlock Cannabinoid Knowledge and save every piece of cannabis resin.

---

# Cannabis Resin

Cannabis resin is a bonus from sieving cured cannabis leaves. Save every piece.

@item mydrugs:cannabis_resin

Resin is needed later for hash. You cannot finish the hash route as soon as you find resin because the stomp crafter is locked behind heavy iron.

> [TIP] Keep a separate chest for cannabis resin. Ten resin press into one hash brick later.

> [GOAL] Store cannabis resin while you continue toward Fermented Knowledge.

---

# Copper Shaping

Cannabinoid Knowledge unlocks copper plate recipes on the Psy Anvil.

@item mydrugs:copper_plate
@item mydrugs:copper_strapping
@item mydrugs:copper_tube
@item mydrugs:wooden_frame

Copper plates become strapping, tubes, trays, and machine parts. This is the start of the workshop tier.

Use copper strapping, a clay vat, a wooden frame, and a stick to build the mixing vat.

@item mydrugs:clay_vat
@item mydrugs:mixing_vat
@item mydrugs:mixing_spatula

> [GOAL] Make copper plates and build a mixing vat.

---

# Fermentation Setup

The first alcohol gate does not require a distiller. Use the mixing vat to make fermented mash, then drink it from a MyDrugs glass bottle.

@item mydrugs:glass_bottle
@item mydrugs:rye
@item mydrugs:malt
@item mydrugs:malt_powder
@item mydrugs:flour

Grind malt into malt powder. Grind wheat into flour when needed. Use water, crop mash, malt powder, and wild yeast in the mixing vat according to JEI. Heat is required for some mash steps.

Mash can begin from rye, wheat, potato, or malt. Rye mash, wheat mash, and potato mash need malt powder to become sweet mash. Malt mash can become sweet mash directly. Sweet mash plus wild yeast becomes fermented mash.

@item mydrugs:rye_mash_bucket
@item mydrugs:wheat_mash_bucket
@item mydrugs:potato_mash_bucket
@item mydrugs:malt_mash_bucket
@item mydrugs:sweet_mash_bucket
@item mydrugs:wild_yeast_bucket
@item mydrugs:fermented_mash_bucket

> [TIP] Use clay vats to store your intermediate fluids and optionally label them. Even later with ethanol, you will need to store a lot of different fluids because at your stage, mixing vat is expensive.

> [GOAL] Produce fermented mash in the mixing vat.

---

# Fermented Knowledge

Fill a MyDrugs glass bottle with fermented mash and drink it after Cannabinoid Knowledge is active.

@item mydrugs:glass_bottle
@item mydrugs:fermented_mash_bucket

Fermented Knowledge unlocks heavy iron on the Psy Anvil and marks the first real industrial jump. It also awards a small insulated-wire clue, but repeatable insulated-wire crafting comes later from Stimulant Knowledge.

@item mydrugs:heavy_iron
@item mydrugs:heavy_iron_plate
@item mydrugs:insulated_wire

> [WARN] Keep the early insulated wires for the centrifuge chain. Spending them on unrelated crafts can slow the route.

> [GOAL] Drink fermented mash and unlock Fermented Knowledge.

---

# Heavy Iron Workshop

With Fermented Knowledge, craft heavy iron and heavy iron plates on the Psy Anvil.

@item mydrugs:heavy_iron
@item mydrugs:heavy_iron_plate
@item mydrugs:mechanical_frame
@item mydrugs:reinforced_casing
@item mydrugs:iron_axle
@item mydrugs:iron_hammer

Heavy iron is used for stronger frames, casings, the stomp crafter, and the first serious machines.

> [TIP] If a machine recipe asks for reinforced casing or mechanical frame, work backward through heavy iron parts in JEI.

> [GOAL] Craft enough heavy iron to build the stomp crafter and begin machine construction.

---

# Stomp Crafter and Hash

Now return to the cannabis resin you saved.

@item mydrugs:stomp_plate
@item mydrugs:stomp_crafter
@item mydrugs:hash_brick
@item mydrugs:hash_piece

Craft stomp plates and the stomp crafter. Press cannabis resin into a hash brick by falling on the block. Split the hash brick into hash pieces at a crafting table.

Put hash pieces in the bang or roll them into a smokable item to unlock Steel Plating Knowledge.

> [WARN] Hash is not skipped by the machine route. Steel Plating Knowledge comes from hash consumption.

> [GOAL] Make hash and unlock Steel Plating Knowledge.

---

# Steel Plating

Steel Plating Knowledge unlocks steel plates on the Psy Anvil.

@item mydrugs:steel_blend
@item mydrugs:steel_ingot
@item mydrugs:steel_plate
@item mydrugs:steel_hammer

Use JEI to reach steel ingots through the current advanced furnace and steel blend recipes. Steel blend can be stomp-crafted from iron and coal dust, then heated into steel. The advanced furnace can also make steel from iron and coal.

Convert steel ingots into steel plates on the Psy Anvil.

Steel plates are a major material gate for stronger machines, seals, tanks, pipes, and late production blocks.

> [GOAL] Build the advanced furnace and craft your first steel plate.

---

# Early Machines

After heavy iron and steel plates, begin assembling the larger processing blocks.

@item mydrugs:advanced_furnace
@item mydrugs:distiller
@item mydrugs:centrifuge
@item mydrugs:fluid_filterer
@item mydrugs:evaporation_tray
@item mydrugs:advanced_mixing_vat

These machines take the mod from hand processing into fluid, gas, and chemical processing. Build them as JEI and advancements point to them.

The advanced furnace is especially important because many later materials need heated processing. The centrifuge and fluid filterer are required for coca and lysergic chemistry.

> [GOAL] Build the advanced furnace, then expand into distillation, filtering, evaporation, and centrifuging.

---

# Material Checklist

Late workshop recipes reuse the same support materials many times.

@item mydrugs:plant_biomass
@item mydrugs:coal_dust
@item mydrugs:activated_coal
@item mydrugs:porous_clay
@item mydrugs:porous_ceramic
@item mydrugs:raw_rubber
@item mydrugs:rubber
@item mydrugs:raw_thick_glass
@item mydrugs:thick_glass
@item mydrugs:glass_tube
@item mydrugs:tight_seal
@item mydrugs:fluid_filter
@item mydrugs:refractory_mix
@item mydrugs:refractory_brick

Plant biomass comes from pulping coffee and drying or evaporating plant material. Coal dust comes from grinding coal or charcoal. Raw rubber comes from resin processing and becomes rubber in the advanced furnace. Thick glass, porous ceramic, seals, filters, and refractory bricks appear repeatedly in machine recipes.

> [TIP] Generic resin, plant biomass, clay, coal dust, glass tubes, copper tubes, rubber, and refractory bricks are common bottlenecks.

> [GOAL] Prepare support materials before starting coca and laboratory chains.

---

# Ores and World Materials

The world adds aluminium, platinum, sulfur, salt, and petroleum.

@item mydrugs:aluminium_ore
@item mydrugs:raw_aluminium
@item mydrugs:aluminium_ingot
@item mydrugs:platinum_ore
@item mydrugs:raw_platinum
@item mydrugs:platinum_ingot
@item mydrugs:sulfur_ore
@item mydrugs:sulfur_powder
@item mydrugs:salt_powder
@item mydrugs:petroleum_bucket

Aluminium and platinum ores generate underground, with deepslate variants lower down. Sulfur ore smelts or blasts into sulfur powder. Salt is found as disks in ocean terrain and can be crafted into salt powder. Petroleum lakes appear in deserts.

Platinum matters for advanced catalytic chemistry. Aluminium matters in late chemical equipment. Petroleum starts the aromatic chain used by late meth chemistry.

> [GOAL] Mine sulfur, aluminium, and platinum before deep laboratory work. Mark desert petroleum when you find it.

---

# Alcohol Distillation

Fermented mash is enough for Fermented Knowledge, but distillation matters for chemistry.

@item mydrugs:fermented_mash_bucket
@item mydrugs:low_wines_bucket
@item mydrugs:raw_alcohol_bucket
@item mydrugs:ethanol_bucket
@item mydrugs:absolute_ethanol_bucket
@item mydrugs:stillage_bucket
@item mydrugs:wastewater_bucket
@item mydrugs:fusel_oil_bucket

Distill fermented mash into low wines and stillage. Distill low wines into raw alcohol and wastewater. Distill raw alcohol into ethanol and fusel oil. Mix ethanol with salt powder to make absolute ethanol.

Absolute ethanol is used in the coca route and in laboratory chemistry.

> [TIP] Do not stop at drinkable mash. The chemical routes need refined alcohol products.

> [GOAL] Produce absolute ethanol before starting coca extraction.

---

# Coca to Stimulant Knowledge

Coca begins after Fermented Knowledge and uses machine processing.

@item mydrugs:coca_leaf
@item mydrugs:dried_coca_leaf
@item mydrugs:coca_paste
@item mydrugs:murky_extract_bucket
@item mydrugs:filtered_extract_bucket
@item mydrugs:cocaine_plate
@item mydrugs:cocaine_powder

Dry coca leaves, grind them into coca paste, then mix coca paste with absolute ethanol to make murky extract. Centrifuge murky extract into filtered extract. Evaporate filtered extract into cocaine plates. Grind cocaine plates into cocaine powder.

Consume cocaine powder to unlock Stimulant Knowledge after Fermented Knowledge is active.

> [WARN] This branch expects alcohol-era machines and fluids. Do not treat coca like the simple tobacco route.

> [GOAL] Consume cocaine powder and unlock Stimulant Knowledge.

---

# Cocaine Use and Crack

Cocaine powder can be consumed directly, rolled into some mixed smoking items, or placed as a powder pile.

@item mydrugs:cocaine_powder
@item mydrugs:cupboard_piece
@item mydrugs:crack_plate
@item mydrugs:crack_shard

To make a cocaine rail, right-click the top of a solid block with cocaine powder to place a powder pile. Use a cardboard piece on the pile to shape it into a rail. Right-click the rail to consume it.

Crack is the later smoking branch. Use the advanced mixing vat and evaporation tray route shown in JEI to make crack plates, then grind them into crack shards.

> [WARN] Cocaine and crack can push addiction, bad trip, and overdose pressure hard. Prepare recovery before repeated use.

> [GOAL] Use cocaine powder for the Stimulant gate, then treat crack as an optional stronger branch.

---

# Stimulant Unlocks

Stimulant Knowledge unlocks insulated wire crafting on the Psy Anvil.

@item mydrugs:insulated_wire
@item mydrugs:control_circuit
@item mydrugs:electric_motor
@item mydrugs:heating_coil
@item mydrugs:condenser_coil
@item mydrugs:electrode_pair

Make rubber, craft insulated wire, then build control circuits and powered machine parts.

This is the point where automation, chemical machines, and advanced laboratory blocks become much easier to connect.

> [TIP] The insulated wire given at Fermented Knowledge was a hint. Stimulant Knowledge is what makes the recipe repeatable.

> [GOAL] Craft insulated wire and your first control circuit.

---

# Pipes and Transfers

Machines can move items, fluids, and gases through separate pipe systems.

@item mydrugs:basic_item_pipe
@item mydrugs:basic_fluid_pipe
@item mydrugs:basic_gas_pipe
@item mydrugs:fast_item_pipe
@item mydrugs:fast_fluid_pipe
@item mydrugs:fast_gas_pipe
@item mydrugs:pipe_wrench
@item mydrugs:pipe_filter_upgrade
@item mydrugs:machine_transfer_upgrade
@item mydrugs:automation_upgrade
@item mydrugs:energy_upgrade

Use the pipe wrench and transfer upgrades to control which side accepts or outputs each resource. Use filters when only specific items, fluids, or gases should pass.

> [TIP] When a machine stops, check output slots, fluid tanks, gas tanks, heat, side configuration, pipe direction, filters, energy, and output space before assuming the recipe is broken.

> [GOAL] Connect at least one machine line with the correct pipe type and transfer settings.

---

# Gas and Chemical Handling

Late recipes use gases and chemical fluids. Build handling blocks before attempting the lysergic or meth chains.

@item mydrugs:gas_tank
@item mydrugs:gas_pump
@item mydrugs:fluid_pump
@item mydrugs:chemical_reactor
@item mydrugs:gasifier
@item mydrugs:electrolyzer
@item mydrugs:advanced_mixing_vat

Chemical recipes often need exact fluid or gas inputs. JEI categories show which machine accepts each input.

The electrolyzer splits water or brine into gases and chemical outputs. The gasifier converts sulfur powder and salt powder into gas routes. The chemical reactor combines gases into intermediates. The advanced mixing vat dissolves gases into acids and combines higher-tier fluids.

> [WARN] Fluids, gases, and items are not interchangeable. A full item inventory will not help if the missing input is in a gas tank.

> [GOAL] Build gas storage, a gas pump, a chemical reactor, and an advanced mixing vat.

---

# Acids and Laboratory Reagents

Lysergic and meth chemistry need acids and activated reagents.

@item mydrugs:brine_bucket
@item mydrugs:hydrochloric_acid_bucket
@item mydrugs:sulfuric_acid_bucket
@item mydrugs:acylating_agent_bucket
@item mydrugs:amino_acid_bucket
@item mydrugs:tryptophan
@item mydrugs:diethylamine_bucket

Make brine from salt powder and water, then electrolyze it for chlorine and sodium hydroxide. Water electrolysis supplies hydrogen and oxygen. Sulfur routes supply sulfur gases. Chemical reactors and the advanced mixing vat turn these into hydrochloric acid and sulfuric acid.

Mix sulfuric acid and hydrochloric acid into acylating agent. Use plant biomass, malt, and water for amino acid, then fluid filter it into tryptophan. Use absolute ethanol and ammoniac for diethylamine.

> [TIP] If an LSD recipe looks blocked, the missing piece is often a gas, acid, or small reagent rather than the obvious crop input.

> [GOAL] Stock hydrochloric acid, sulfuric acid, acylating agent, tryptophan, and diethylamine.

---

# Lysergic Route

The lysergic route starts from fungal culture, rye, ergot, and laboratory fluids.

@item mydrugs:growth_chamber
@item mydrugs:biochemical_reactor
@item mydrugs:fungal_fiber
@item mydrugs:fungal_culture
@item mydrugs:infected_rye
@item mydrugs:ergot
@item mydrugs:ergotamine
@item mydrugs:ergotamine_bucket
@item mydrugs:lysergic_acid_bucket
@item mydrugs:activated_lysergic_acid_bucket
@item mydrugs:lsd_bucket
@item mydrugs:lsd_drop

Use the growth chamber to turn ergot and biomass into fungal culture. Use fungal culture with rye seeds to grow infected rye and harvest ergot.

Use ergot and tryptophan in the biochemical reactor to make ergotamine. Use hydrochloric acid and water to process it into lysergic acid, then use acylating agent to activate it. Combine activated lysergic acid with diethylamine to make LSD fluid.

To finish the route, drop cardboard pieces on the ground and right-click the dropped stack with a MyDrugs glass bottle containing LSD. Each small dose of LSD and one cupboard piece becomes one LSD Drop.

Lysergic Knowledge is granted by consuming the LSD Drop after Stimulant Knowledge is active.

> [GOAL] Unlock Lysergic Knowledge.

---

# Advanced Control Circuits

Lysergic Knowledge unlocks advanced control circuits on the Psy Anvil.

@item mydrugs:advanced_control_circuit
@item mydrugs:reaction_core
@item mydrugs:catalyst_bed
@item mydrugs:packed_column
@item mydrugs:pressure_seal
@item mydrugs:valve
@item mydrugs:membrane
@item mydrugs:injector_nozzle

These parts push the mod into late laboratory machinery: catalytic reforming, cracking, aromatic extraction, biochemical systems, and high-tier chemical chains.

> [TIP] Advanced circuits are also required for psychotrope and mycelial components later.

> [GOAL] Craft an advanced control circuit.

---

# Petroleum and Aromatics

The meth route depends on petroleum, aromatics, and gas chemistry.

@item mydrugs:petroleum_bucket
@item mydrugs:naphtha_bucket
@item mydrugs:coal_tar_bucket
@item mydrugs:light_oil_bucket
@item mydrugs:reformate_bucket
@item mydrugs:btx_mix_bucket
@item mydrugs:benzene_bucket
@item mydrugs:toluene_bucket
@item mydrugs:xylene_bucket
@item mydrugs:sulfolane_bucket

Distill petroleum to get naphtha and industrial byproducts. Coal tar can also be distilled into light oil. Run naphtha through steam cracking and catalytic reforming. Use aromatic extraction to produce BTX mix, then split BTX mix in the BTX fractionation tower.

Benzene, propylene, acetone, chloroacetone, methylamine, and phenylacetone are the key names to follow in JEI for the meth route.

@item mydrugs:steam_cracker
@item mydrugs:catalytic_reformer
@item mydrugs:aromatic_extractor
@item mydrugs:btx_fractionation_tower

> [TIP] This branch is machine-heavy. Build the tower, reformer, extractor, and cracker before trying to batch meth precursors.

> [GOAL] Produce benzene and propylene for late meth chemistry.

---

# Overclocked Route

The meth route follows Lysergic Knowledge and uses the highest chemical machinery.

@item mydrugs:methanol_bucket
@item mydrugs:methylamine_bucket
@item mydrugs:acetone_bucket
@item mydrugs:chloroacetone_bucket
@item mydrugs:phenylacetone_bucket
@item mydrugs:methamphetamine_bucket
@item mydrugs:meth_shard
@item mydrugs:meth_powder

Use JEI to build the precursor chain through gases, reactors, reformers, aromatics, and evaporation. Meth shards are the visible crystallized product. Grind them into meth powder for the valid smoking route.

Consume meth through a valid smoking route after Lysergic Knowledge is active to unlock Overclocked Knowledge.

> [WARN] Meth is a late route, not a shortcut. Start it only after advanced circuits and chemical handling are stable.

> [GOAL] Grind meth shards into meth powder, smoke it, and unlock Overclocked Knowledge.

---

# Mushrooms and Mycelial Knowledge

Magic mushrooms are available earlier than their knowledge gate.

@item mydrugs:magic_mushroom
@item mydrugs:magic_mushroom_powder
@item mydrugs:mycelial_resonator

You can find mushrooms in psychedelic terrain and use them as a psychedelic item, but Mycelial Knowledge is only granted after Overclocked Knowledge is active.

Grind mushrooms into powder for late recipes. The mycelial resonator uses mushroom powder, amethyst, redstone, and an advanced control circuit on the Psy Anvil. Its recipe is available from Overclocked Knowledge.

> [WARN] Eating mushrooms too early does not open the mycelial tree. The knowledge is deferred until the required gate is met.

> [GOAL] After Overclocked Knowledge, consume a magic mushroom to unlock Mycelial Knowledge.

---

# Psychedelic Exploration Tools

Some late ingredients are tied to exploration rather than simple crafting.

@item mydrugs:vanilla_biome_finder
@item mydrugs:shroom_harvester
@item mydrugs:magic_mushroom
@item mydrugs:magic_mushroom_powder
@item mydrugs:psychedelic_grass
@item mydrugs:psychedelic_mycelium
@item mydrugs:calming_spores
@item mydrugs:dreamcap_spores

The vanilla biome finder helps search for target biomes. The shroom harvester is designed for mushroom gathering. Psychedelic terrain contains special grass, mycelium, and mushroom life.

> [TIP] Bring recovery items before exploring psychedelic terrain or testing mushrooms.

> [GOAL] Find psychedelic terrain and collect mushrooms for the Mycelial and Psy Mixer branches.

---

# Psy Mixer Rituals

The Psy Mixer is a multiblock ritual system using a formed core and ritual parts.

@item mydrugs:painted_clay_bowl
@item mydrugs:psychotropic_pigment
@item mydrugs:ritual_resin
@item mydrugs:ritual_threads
@item mydrugs:ritual_bark
@item mydrugs:charcoal_glyph_block
@item mydrugs:hanging_vine_bundle
@item mydrugs:woven_vine_frame
@item mydrugs:mycelial_padding
@item mydrugs:unstable_residue

Psy Mixer recipes can require knowledge, a specific drug history, a catalyst, a stabilizer, and a vessel. Failure may return unstable residue or another failure item.

> [TIP] Use JEI's Psy Mixer category before building or activating a ritual setup.

> [GOAL] Build ritual materials after the workshop and knowledge routes are stable.

---

# Psy Mixer Recipes

Current ritual outputs include brightened cannabis powder, soothing tobacco blend, and defiant spirit bottle.

@item mydrugs:brightened_cannabis_powder
@item mydrugs:soothing_tobacco_blend
@item mydrugs:defiant_spirit_bottle
@item mydrugs:inner_demon_remains
@item mydrugs:aloe_vera
@item mydrugs:charged_sinew
@item mydrugs:fractured_impulse
@item mydrugs:charged_core
@item mydrugs:broken_courage

Brightened cannabis powder requires Cannabinoid Knowledge and enough lifetime weed exposure. Soothing tobacco blend requires Nicotinic Knowledge and tobacco exposure. Defiant spirit bottle requires Fermented Knowledge and alcohol history.

The recipe details are intentionally ritual-specific, so follow the JEI Psy Mixer page for catalyst, stabilizer, vessel, and failure output.

> [WARN] Psy Mixer recipes care about your drug history, not only the items on the structure.

> [GOAL] Try the tobacco and cannabis rituals first, then the fermented ritual.

---

# Psychotrope Research

Psychotrope systems are endgame research built from advanced control circuits and high-tier components.

@item mydrugs:psychotrope_lens
@item mydrugs:psychotrope_component
@item mydrugs:psychotrope_core
@item mydrugs:recovery_anchor

Craft the lens first, then components, then the core. The core belongs to a formed multiblock that can generate psychotrope energy.

Psychotrope systems do not block tobacco, cannabis, fermented mash, hash, steel plating, stimulant, lysergic, overclocked, or mycelial progression. They are a late research and power branch.

> [GOAL] Craft a psychotrope lens after advanced control circuits are available.

---

# Psychotrope Energy

The psychotrope core can convert drug value into energy for machines.

@item mydrugs:psychotrope_core
@item mydrugs:energy_upgrade
@item mydrugs:automation_upgrade
@item mydrugs:meth_powder
@item mydrugs:lsd_drop
@item mydrugs:crack_shard
@item mydrugs:cocaine_powder
@item mydrugs:cannabis_powder

Different drugs have different psychotrope values. Late drugs such as meth and LSD are far stronger inputs than early cannabis. Energy upgrades and automation upgrades help machines use this late power system.

Mutation machines also use psychotrope energy. They drain energy over time, so they need an energy upgrade or automation support and a steady supply from the psychotrope core. The Gene Extractor is the cheapest mutation machine. The CRISPR-CAS9 Combinator, Bacterial Incubator, and Hemogenic Infuser are much more expensive.

> [WARN] Feeding your best drugs into energy is a resource trade. Do it when your production chain can replace them.

> [GOAL] Generate psychotrope energy and use it to support late machine lines, then start mutation work.

---

# Bottles Syringes and Fluid Use

Bottles and syringes are utility tools, but they do not replace the main unlock path. Syringes also become important for the mutation branch.

@item mydrugs:glass_bottle
@item mydrugs:syringe
@item mydrugs:lsd_bottle
@item mydrugs:blood_bucket
@item mydrugs:autoclave

The MyDrugs glass bottle can hold mod fluids and drink fluids that are marked as drinkable, such as coffee and fermented mash. It is also used to convert LSD fluid plus cupboard pieces into LSD Drops.

The syringe can draw blood and can later carry mutagenic blood. New syringes are dirty by default. Use the Autoclave with psychotrope energy to sterilize empty dirty syringes before blood handling or mutation injection.

> [WARN] If a fluid will not drink from the bottle, it may be a processing fluid rather than a drinkable drug fluid.

> [WARN] Drawing blood from yourself with a dirty syringe can start infection. Injecting mutagenic blood with a dirty syringe rejects the payload and starts infection.

> [GOAL] Use bottles for drinkable fluids and LSD drop creation. Keep sterile syringes ready for mutation work.

---

# Mutation Overview

Mutations are an endgame biological progression branch. They use fictional ADN data, psychotrope energy, blood handling, and dangerous injections to change how your body reacts to the mod's drug systems.

@item mydrugs:adn_scraper
@item mydrugs:adn_scrap
@item mydrugs:adn_gene
@item mydrugs:mutation_vector
@item mydrugs:mutagenic_blood_vial
@item mydrugs:syringe

The full route is: scrape ADN -> extract genes -> combine compatible genes -> incubate a mutation vector -> infuse it with blood -> load a sterile syringe -> inject -> assimilate the mutation over time.

Mutations can reduce negative visual distortion, internal drug damage, addiction gain, mental symptoms, withdrawal symptoms, overdose pressure, ritual instability, infection progression, and gene instability. Pleasure Sensitivity improves positive drug effects, but it also increases addiction pressure slightly.

> [WARN] Mutations are powerful but not free. Dirty injections can cause infection, infection can damage you, and severe infection can erase mutations.

> [GOAL] Build a stable psychotrope energy setup before trying mutations.

---

# ADN Scraping

Use the ADN Scraper on living mobs to collect ADN Scrap. Right-click air with the scraper to sample yourself.

@item mydrugs:adn_scraper
@item mydrugs:adn_scrap

Mob ADN is deterministic: the same mob UUID produces the same scrap stats, while different mobs of the same type can roll different stats. Basic mobs usually have only two or three low stats. Mythic sources can have more stats and better odds.

Player ADN is different. Scraping a player copies that player's current active mutation stats. If the player has no mutations, the scrap is empty. This lets player ADN reflect the body as it exists now, not a fresh random profile.

You cannot inject mutation payloads sourced from your own player UUID. Self-scraping is for sharing, study, or future systems, not for looping your own stats back into yourself.

ADN Scrap tooltips show source, rarity, signature, and stat percentages. Advanced tooltips show source UUID and improbability scores.

> [TIP] Basic mob scraps are supposed to be weak. Use them for early testing, then hunt rarer sources when the machine chain is ready.

> [GOAL] Collect several scraps from different sources before building the extractor.

---

# Gene Extractor

The Gene Extractor turns ADN Scrap into single-stat ADN Genes.

@item mydrugs:gene_extractor
@item mydrugs:adn_scrap
@item mydrugs:adn_gene
@item mydrugs:energy_upgrade

Install energy support, feed the machine from psychotrope energy, place one ADN Scrap in the input, and keep the three output slots clear. Extraction takes time and pauses when energy is missing.

The extractor chooses up to three unique stats from the scrap. If the scrap has fewer than three stats, it only outputs those stats. No duplicate genes are created from one extraction.

> [TIP] Extraction selection is random. If you want a specific stat from a good source, collect multiple scraps from that source.

> [GOAL] Extract a set of single-stat ADN Genes from several different sources.

---

# CRISPR-CAS9 Combinator

The CRISPR-CAS9 Combinator merges two ADN Genes into one multi-stat gene.

@item mydrugs:crispr_cas9_combinator
@item mydrugs:adn_gene
@item mydrugs:energy_upgrade

Genes from the same source UUID cannot be combined. This means you need genes from different mobs or players to build larger profiles.

Successful combinations preserve all sources and stats. If both genes contain the same stat, the stat values are added together and clamped at 100 percent. For example, 2 percent Genetic Stability plus 1 percent Genetic Stability becomes 3 percent Genetic Stability.

Combination can fail. Failure consumes both inputs and creates a broken statless ADN Gene. More complex genes have a higher failure chance.

> [WARN] Do not feed your only good gene into a risky high-complexity combine unless you can afford to lose it.

> [GOAL] Combine compatible genes from different sources into a profile worth cultivating.

---

# Incubation and Infusion

The Bacterial Incubator converts a valid ADN Gene into a Mutation Vector. The Hemogenic Infuser mixes that vector with blood to create a Mutagenic Blood Vial.

@item mydrugs:bacterial_incubator
@item mydrugs:nutrient_gel
@item mydrugs:mutation_vector
@item mydrugs:hemogenic_infuser
@item mydrugs:mutagenic_blood_vial

The incubator requires a non-broken ADN Gene, Nutrient Gel, output space, and psychotrope energy. The vector keeps the gene's sources, stats, assimilation difficulty, and rejection risk.

The Hemogenic Infuser requires a Mutation Vector and a syringe filled with blood. It outputs a Mutagenic Blood Vial and empties the blood syringe, leaving it dirty.

> [TIP] Higher stat counts and stronger stat values make vectors harder to assimilate and more likely to reject.

> [GOAL] Turn one combined gene into a Mutation Vector, then into a Mutagenic Blood Vial.

---

# Sterile Injection

Use the Autoclave to sterilize empty dirty syringes, then load a Mutagenic Blood Vial into a sterile empty syringe.

@item mydrugs:autoclave
@item mydrugs:syringe
@item mydrugs:mutagenic_blood_vial

A syringe loaded with mutagenic blood stays sterile until injection. It uses the filled syringe texture and shows the mutation payload in its tooltip.

Hold use with the loaded syringe to inject yourself. Sterile injection starts assimilation: mutation values move gradually toward their target values over time instead of applying instantly. If you already have the same stat, the new target is averaged with the injected value and rounded to the nearest percent. Genetic Stability bends that average toward the better value. The syringe becomes empty and dirty afterward.

> [WARN] Only sterile empty syringes can be safely loaded for mutation injection.

> [GOAL] Sterilize a syringe, load mutagenic blood, and inject when you are ready to wait through assimilation.

---

# Infection and Mutation Risk

Dirty syringe use can start infection. Infection is dangerous and can undo mutation progress.

@item mydrugs:syringe
@item mydrugs:autoclave
@item mydrugs:personal_diary
@item mydrugs:herbal_tea
@item mydrugs:calming_mixture

Dirty self blood draw can infect you. Dirty mutagenic injection starts infection and rejects the payload. Infection progresses through contamination, infection, sepsis, and collapse. It can apply nausea or confusion pressure, damage you, decay mutations, or remove mutations at severe stages.

Infection Resistance slows infection progression and mutation loss, but it does not make dirty injections safe. Health Stability helps with internal mutation and infection damage, but it is not armor against normal combat or environmental damage.

> [WARN] If infection reaches the worst stages, your mutations can collapse completely.

> [GOAL] Autoclave syringes before risky use and keep recovery support near your mutation lab.

---

# Mutation Effects

Assimilated mutations change existing drug, symptom, dose, visual, and work-speed systems.

Visual Accuracy reduces negative visual distortion, but it does not reduce Gamma Boost. Health Stability reduces internal drug-system damage. Addiction Resistance reduces addiction gain. Mental Strength reduces mental symptoms and bad-trip pressure. Withdrawal Resilience reduces physical withdrawal symptoms.

Metabolic Control helps dangerous dose states resolve more safely. Ritual Neural Sync improves ritual and manual-machine compatibility. Infection Resistance slows infection. Genetic Stability improves assimilation stability and future mutation safety. Pleasure Sensitivity boosts positive drug effects, but it also slightly increases addiction pressure.

> [TIP] A balanced profile is safer than chasing one huge number. The strongest genes are expensive, risky, and slow to assimilate.

> [GOAL] Build mutations that match the risks you actually face: visuals, addiction, withdrawal, infection, or dose pressure.

---

# Recovery and Addiction

Addiction and recovery are active systems. Repeated use can build addiction, withdrawal, bad trips, sleep problems, and overdose pressure.

@item mydrugs:personal_diary
@item mydrugs:headphones
@item mydrugs:herbal_tea
@item mydrugs:calming_mixture
@item mydrugs:sleeping_aid
@item mydrugs:overdose_antidote
@item mydrugs:therapist_desk
@item mydrugs:recovery_anchor

Recovery items reduce or manage stress, withdrawal, addiction pressure, sleep blocking, overdose danger, and mutation-adjacent risk. The personal diary, headphones, teas, calming mixture, sleeping aid, antidote, therapist desk, and recovery anchor all matter.

> [WARN] Recovery is not just flavor. It is part of surviving long production chains, especially stimulant, crack, meth, mutation injection, infection, and psychedelic experiments.

> [GOAL] Keep at least one recovery option available before repeated drug use.

---

# Diary Headphones Therapy Anchor

The personal diary gives a short calming recovery window and helps you read your current state. Headphones are a toggleable support item while carried. Herbal tea and calming mixture reduce stress and withdrawal. Sleeping aid helps when sleep is blocked. Overdose antidote reduces dose pressure and interrupts overdose danger.

The therapist desk creates a therapist villager point of interest. Therapy is limited over time, but it can reduce withdrawal, addiction, stress, and improve resilience.

The recovery anchor creates a safe-zone style recovery support area.

@item mydrugs:personal_diary
@item mydrugs:headphones
@item mydrugs:herbal_tea
@item mydrugs:calming_mixture
@item mydrugs:sleeping_aid
@item mydrugs:overdose_antidote
@item mydrugs:therapist_desk
@item mydrugs:recovery_anchor

> [TIP] Use the diary when you are unsure why you feel good or bad. It summarizes live stress, withdrawal, symptoms, dose pressure, and recovery supports.

> [GOAL] Build a recovery corner near your lab before late-game use chains.

---

# Space Food and Side Content

Space food variants are side content generated from many vanilla foods.

@item mydrugs:space_apple
@item mydrugs:space_bread
@item mydrugs:space_carrot
@item mydrugs:space_cooked_beef
@item mydrugs:space_golden_apple

They are not the backbone of the drug knowledge chain. Treat them as side content unless JEI or another system points to a specific use.

Other visible side materials include lightning, thunder, impulse, courage, core, and sinew-themed ritual ingredients. These mainly belong to ritual or special-item experiments.

@item mydrugs:thunder_bottle
@item mydrugs:lightning_bottle
@item mydrugs:fractured_impulse
@item mydrugs:broken_courage
@item mydrugs:charged_core
@item mydrugs:charged_sinew

> [TIP] If a side item has no clear progression role, check JEI first and keep a sample instead of mass-crafting it.

---

# When You Are Stuck

Check these in order.

## Advancement Tree

The parent advancement usually shows the intended previous gate.

## JEI Category

Search the item, fluid, gas, and machine. Many routes move between several recipe categories.

## Knowledge Gate

Confirm the needed Psy Knowledge is active. A visible recipe can still be locked.

## Inputs and Outputs

Check item slots, fluid tanks, gas tanks, heat, pipe direction, transfer upgrades, filters, energy, and output space.

## Correct Consumption Form

Some items are ingredients but not direct consumables. Meth shards should be ground into meth powder. LSD fluid should be converted into LSD Drops. Coca can be powder or rail. Fresh leaves usually do not grant knowledge.

## Prototype and Side Content

Some research, ritual, or side systems are visible before every connection is obvious. The main route through coffee, tobacco, cannabis, fermented mash, hash, steel plating, coca, LSD, meth, mushrooms, and psychotrope research is the intended complete path.

> [TIP] When in doubt, follow the last knowledge you unlocked. The next locked Psy Anvil or machine recipe usually tells you where to go.

---

# Progression Summary

Quick route:

Break grass -> grow crops -> coffee -> Caffeine Knowledge -> Psy Receptacle -> strip logs for resin -> treated planks -> drying rack -> grinding bowl -> tobacco handful -> Nicotinic Knowledge -> Psy Anvil -> iron mesh -> sieve -> cured cannabis -> dried cannabis plus resin -> cannabis powder -> Cannabinoid Knowledge -> copper plates -> mixing vat -> fermented mash -> Fermented Knowledge -> heavy iron -> stomp crafter -> hash -> Steel Plating Knowledge -> steel plates -> advanced furnace and machines -> absolute ethanol -> coca paste and extracts -> cocaine powder -> Stimulant Knowledge -> insulated wire -> control circuits -> acids and gases -> fungal culture and ergot -> LSD Drop -> Lysergic Knowledge -> advanced control circuits -> petroleum and aromatic chemistry -> meth powder -> Overclocked Knowledge -> magic mushroom consumption -> Mycelial Knowledge -> psychotrope research -> ADN Scraper -> Gene Extractor -> CRISPR-CAS9 Combinator -> Bacterial Incubator -> Hemogenic Infuser -> Autoclave -> sterile mutagenic injection.

Parallel branches: recovery, rolling, bang use, cocaine rails, crack, alcohol distillation, pipes and transfers, gas handling, Psy Mixer rituals, psychedelic exploration, space food, psychotrope energy, and mutation management.

> [GOAL] Complete every knowledge gate, then use psychotrope energy, rituals, and mutations as the endgame sandbox.
