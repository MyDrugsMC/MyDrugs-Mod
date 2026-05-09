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

Welcome. This book explains the current progression path for MyDrugs: what to build, what to consume, and which knowledge gate should open next.

Keep JEI and the advancement tree open beside this guide. JEI is still the source for exact ingredients and fluid amounts. This guide explains the order. Open [[Table of Contents|the table of contents]] when you want to jump to a section.

> [TIP] Right-click the guide at any time to reopen it. The guide item is not consumed.

> [GOAL] Find seeds, start crops, and prepare for the first tobacco route.

---

# The Current Spine

The current main route is:

Seeds → [[Coffee First|coffee]] → Caffeine Knowledge → Psy Receptacle → resin from logs → drying and grinding → tobacco → Nicotinic Knowledge → Psy Anvil → iron mesh and sieve → cannabis → Cannabinoid Knowledge → copper shaping → mixing vat → fermented mash → Fermented Knowledge → heavy iron → stomp crafter → hash → Steel Plating Knowledge → steel plates and machines → coca → Stimulant Knowledge → circuits → lysergic route → meth route → Overclocked Knowledge → mushrooms → Mycelial Knowledge → psychotrope research.

> [WARN] Older notes may say hash comes before heavy iron. In the current recipes, the stomp crafter needs heavy iron, so hash comes after Fermented Knowledge.

> [TIP] If a recipe is visible but refuses to run, check knowledge first. Most locked recipes are waiting for the next knowledge gate.

---

# Coffee First

Coffee is now the first discovery branch. It teaches Caffeine Knowledge, and that first caffeine imprint awards the Psy Receptacle.

@item mydrugs:coffee_cherries
@item mydrugs:manual_coffee_pulper
@item mydrugs:wet_coffee_bean
@item mydrugs:coffee_bean
@item mydrugs:coffee_powder
@item mydrugs:clay_vat
@item mydrugs:cup
@item mydrugs:coffee_cup
@item mydrugs:psy_receptacle

Pulp coffee cherries in the manual coffee pulper to get wet coffee beans and plant biomass. Dry the wet beans, then grind coffee beans into powder.

The reliable early route is the heated clay vat. Put a lit campfire, soul campfire, fire, or soul fire under a clay vat. Add water, then use coffee powder on the heated vat. Each powder brews 250 mB of water into 250 mB coffee. Craft a cup from one brick, then right-click the coffee-filled vat with the cup to make a coffee cup.

The mixing vat route still works later: coffee powder plus 250 mB water in a heated mixing vat produces 250 mB coffee.

> [TIP] Caffeine Knowledge awards the Psy Receptacle. Nicotinic Knowledge still matters, but it no longer gives the receptacle.

> [GOAL] Brew coffee, drink a coffee cup, unlock Caffeine Knowledge, and keep the Psy Receptacle for the Psy Anvil route.

@link Psy Receptacle and Psy Anvil|Continue to the Psy Receptacle

---

# Seeds and First Crops

Break short grass, tall grass, ferns, and large ferns without shears to find crop seeds. Tobacco, cannabis, coca, rye, malt, and coffee seeds can all appear from these early drops.

@item mydrugs:tobacco_seeds
@item mydrugs:cannabis_seeds
@item mydrugs:coca_seeds
@item mydrugs:rye_seeds
@item mydrugs:malt_seeds
@item mydrugs:coffee_seeds

Plant seeds on farmland like vanilla crops. Keep a separate row for each crop because later routes need steady supplies, not one-off harvests.

> [GOAL] Grow tobacco, cannabis, rye, malt, coca, and coffee if you have the seeds.

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

The drying rack converts fresh or wet materials into dried forms. The grinding bowl plus grinding tool turns dried materials into powders, handfuls, paste, or other intermediates.

> [WARN] The sieve is not truly a first-minute station anymore. It needs iron mesh, and iron mesh needs Nicotinic Knowledge through the Psy Anvil.

> [GOAL] Build a drying rack and grinding bowl, then dry your first tobacco leaves.

---

# Tobacco to Nicotinic Knowledge

Tobacco is the first intended knowledge gate.

@item mydrugs:tobacco_leaf
@item mydrugs:dried_tobacco_leaf
@item mydrugs:tobacco_handful

Dry tobacco leaves on the drying rack. Grind dried tobacco leaves in the grinding bowl to obtain tobacco handfuls. Put tobacco handfuls in the bang or roll three of them into a cigarette, then smoke the result to gain Nicotinic Knowledge.

> [GOAL] Consume tobacco and unlock Nicotinic Knowledge.

> [WARN] Fresh tobacco leaf does not grant the knowledge. Process it first.

The Psy Receptacle comes from [[Coffee First|Caffeine Knowledge]], not from Nicotinic Knowledge. Tobacco still unlocks Nicotinic Knowledge and opens the first tobacco-gated Psy Anvil recipes.

---

# Rolling and Smoking

The roller combines paper, a cigarette filter, and three rolling ingredients. Three tobacco ingredients make a cigarette. Mixed or non-tobacco smoking ingredients make a joint.

@item mydrugs:roller
@item mydrugs:cigaret_filter
@item mydrugs:cigaret
@item mydrugs:joint

The bang can hold a smoking item and consume it through the smoking route.

@item mydrugs:bang

> [TIP] Tobacco handfuls, cannabis powder, hash pieces, cocaine powder, and crack shards are rolling ingredients.

> [WARN] The output depends on the three ingredients inside the roller, not only on the last item inserted.

---

# Psy Receptacle and Psy Anvil

After [[Coffee First|Caffeine Knowledge]] gives you the Psy Receptacle and tobacco gives [[Tobacco to Nicotinic Knowledge|Nicotinic Knowledge]], use the receptacle to craft the Psy Anvil.

@item mydrugs:psy_receptacle
@item mydrugs:psy_anvil
@item mydrugs:stone_hammer

The Psy Anvil handles knowledge-gated recipes. Place the required ingredients, then activate the anvil with the proper hammer or tool as shown by JEI.

## If the anvil refuses

Check the ingredient list, the active knowledge, and the tool requirement. Recipes can be visible before you are allowed to complete them.

> [GOAL] Craft the Psy Anvil and use it for your first Nicotinic recipe.

---

# Iron Mesh and the Sieve

The first important Psy Anvil product is iron mesh.

@item mydrugs:iron_mesh
@item mydrugs:sieve

Iron mesh requires Nicotinic Knowledge. Once crafted, use it with copper, sticks, and a wooden frame to make the sieve.

The sieve is essential for the cannabis route because it separates cured cannabis into dried cannabis leaf and cannabis resin.

> [GOAL] Craft iron mesh, then build the sieve.

> [TIP] Do not spend all your early copper. The sieve, copper shaping, trays, tubes, and vats all compete for it.

---

# Cannabis Preparation

Cannabis is the second knowledge route, but it has more steps than tobacco.

@item mydrugs:cannabis_leaf
@item mydrugs:cured_cannabis_leaf
@item mydrugs:dried_cannabis_leaf
@item mydrugs:cannabis_powder

Dry cannabis leaves into cured cannabis leaves. Run cured cannabis through the sieve. The main output is dried cannabis leaf, with cannabis resin as a bonus output. Grind dried cannabis leaf into cannabis powder.

Put cannabis powder in the bang or roll it into a smokable item to gain Cannabinoid Knowledge.

> [GOAL] Unlock Cannabinoid Knowledge.

> [WARN] Sieve cured cannabis before grinding. Grinding the wrong stage skips the resin route.

---

# Cannabis Resin

Cannabis resin is a bonus from sieving cured cannabis leaves. Save every piece.

@item mydrugs:cannabis_resin

Resin is needed later for hash. You cannot finish the hash route as soon as you find resin because the stomp crafter is locked behind heavy iron.

> [TIP] Keep a separate chest for cannabis resin. Ten resin make one hash brick later.

> [GOAL] Store cannabis resin while you continue toward Fermented Knowledge.

---

# Copper Shaping

Cannabinoid Knowledge unlocks copper plate recipes on the Psy Anvil.

@item mydrugs:copper_plate
@item mydrugs:copper_strapping
@item mydrugs:copper_tube

Copper plates become strapping, tubes, trays, and machine parts. This is the start of the workshop tier.

Use copper strapping, a clay vat, a wooden frame, and a stick to build the mixing vat.

@item mydrugs:clay_vat
@item mydrugs:mixing_vat

> [GOAL] Make copper plates and build a mixing vat.

---

# Fermentation Setup

The first alcohol gate does not require a distiller. Use the mixing vat to make fermented mash, then drink it from a MyDrugs glass bottle.

@item mydrugs:glass_bottle
@item mydrugs:rye
@item mydrugs:malt
@item mydrugs:malt_powder

Grind malt into malt powder. Use water, crop mash, malt powder, and wild yeast in the mixing vat according to JEI. Heat is required for some mash steps.

> [TIP] Wild yeast is made in the mixing vat from flour and water.

> [GOAL] Produce fermented mash in the mixing vat.

---

# Fermented Knowledge

Fill a MyDrugs glass bottle with fermented mash and drink it after Cannabinoid Knowledge is active.

Fermented Knowledge unlocks heavy iron on the Psy Anvil and marks the first real industrial jump. It also gives you a small insulated-wire clue, but normal insulated-wire crafting comes later.

> [WARN] Keep the insulated-wires to craft the centrifuge, and only the centrifuge ! Crafting anything else will break the crafting chain

@item mydrugs:heavy_iron
@item mydrugs:heavy_iron_plate
@item mydrugs:insulated_wire

> [GOAL] Drink fermented mash and unlock Fermented Knowledge.

> [WARN] Distillation improves the alcohol chain later, but fermented mash is the practical first unlock for this gate.

---

# Heavy Iron Workshop

With Fermented Knowledge, craft heavy iron and heavy iron plates on the Psy Anvil.

@item mydrugs:heavy_iron
@item mydrugs:mechanical_frame
@item mydrugs:reinforced_casing

Heavy iron is used for stronger frames, casings, the stomp crafter, and the first serious machines.

> [TIP] If a machine recipe asks for reinforced casing or mechanical frame, work backward through heavy iron parts in JEI.

> [GOAL] Craft enough heavy iron to build the stomp crafter and start machine construction.

---

# Stomp Crafter and Hash

Now return to the cannabis resin you saved.

@item mydrugs:stomp_plate
@item mydrugs:stomp_crafter
@item mydrugs:hash_brick
@item mydrugs:hash_piece

Craft stomp plates and the stomp crafter. Press ten cannabis resin into a hash brick. Split the hash brick into hash pieces at a crafting table.

Put hash pieces in the bang or roll them into a smokable item to unlock Steel Plating Knowledge.

> [GOAL] Make hash and unlock Steel Plating Knowledge.

> [WARN] Hash is not skipped by the machine route. Steel Plating Knowledge comes from hash consumption.

---

# Steel Plating

Steel Plating Knowledge unlocks steel plates on the Psy Anvil.

@item mydrugs:steel_blend
@item mydrugs:steel_ingot
@item mydrugs:steel_plate

Use JEI to reach steel ingots through the current furnace and steel-blend recipes. Then convert steel ingots into steel plates on the Psy Anvil.

Steel plates are a major material gate for stronger machines, seals, tanks, pipes, and late production blocks.

To craft them you will have to craft the advanced furnace. Have a greate mining session !

> [GOAL] Craft your first steel plate.
---

# Early Machines

After heavy iron and steel plates, begin assembling the larger processing blocks.

@item mydrugs:advanced_furnace
@item mydrugs:distiller
@item mydrugs:centrifuge
@item mydrugs:fluid_filterer
@item mydrugs:advanced_mixing_vat

These machines take the mod from hand processing into fluid, gas, and chemical processing. Build them as JEI and advancements point to them.

> [TIP] The advanced furnace is especially important because many later materials need heated processing.

> [GOAL] Build the advanced furnace, then expand into distillation, filtering, and centrifuging.

---

# Material Checklist

Late workshop recipes reuse the same support materials many times.

@item mydrugs:activated_coal
@item mydrugs:porous_ceramic
@item mydrugs:thick_glass
@item mydrugs:rubber
@item mydrugs:tight_seal
@item mydrugs:fluid_filter

If progression feels blocked, search these ingredients in JEI and craft a small stockpile.

> [TIP] Generic resin, plant biomass, clay, coal dust, glass tubes, and copper tubes are common bottlenecks.

> [GOAL] Prepare support materials before starting coca and laboratory chains.

---

# Coca to Stimulant Knowledge

Coca begins after Fermented Knowledge and uses machine processing.

@item mydrugs:coca_leaf
@item mydrugs:dried_coca_leaf
@item mydrugs:coca_paste
@item mydrugs:cocaine_plate
@item mydrugs:cocaine_powder

Dry coca leaves, grind them into coca paste, then follow JEI through the extract, centrifuge, evaporation tray, and grinding steps. Cocaine powder is the main Stimulant Knowledge unlock.

> [GOAL] Consume cocaine powder and unlock Stimulant Knowledge.

> [WARN] This branch expects alcohol-era machines and fluids. Do not treat coca like the simple tobacco route.

---

# Stimulant Unlocks

Stimulant Knowledge unlocks insulated wire crafting on the Psy Anvil.

@item mydrugs:insulated_wire
@item mydrugs:control_circuit
@item mydrugs:electric_motor
@item mydrugs:heating_coil

Make rubber, craft insulated wire, then build control circuits and powered machine parts.

This is the point where automation and chemical machines become much easier to connect.

> [GOAL] Craft insulated wire and your first control circuit.

> [TIP] The insulated wire given at Fermented Knowledge was a hint. Stimulant Knowledge is what makes the recipe repeatable.

---

# Pipes and Transfers

Machines can move items, fluids, and gases through separate pipe systems.

@item mydrugs:basic_item_pipe
@item mydrugs:basic_fluid_pipe
@item mydrugs:basic_gas_pipe
@item mydrugs:pipe_wrench
@item mydrugs:pipe_filter_upgrade
@item mydrugs:machine_transfer_upgrade

Use the pipe wrench and transfer upgrades to control which side accepts or outputs each resource.

> [TIP] When a machine stops, check output slots, fluid tanks, gas tanks, pipe direction, and filters before assuming the recipe is broken.

> [GOAL] Connect at least one machine line with the correct pipe type.

---

# Gas and Chemical Handling

Late recipes use gases and chemical fluids. Build the handling blocks before attempting the lysergic or meth chains.

@item mydrugs:gas_tank
@item mydrugs:gas_pump
@item mydrugs:fluid_pump
@item mydrugs:chemical_reactor
@item mydrugs:gasifier
@item mydrugs:electrolyzer

Chemical recipes often need exact fluid or gas inputs. JEI categories show which machine accepts each input.

> [WARN] Fluids, gases, and items are not interchangeable. A full item inventory will not help if the missing input is a gas tank.

> [GOAL] Build a basic gas storage and chemical reactor setup.

---

# Lysergic Route

The lysergic route starts from fungal culture, rye, ergot, and laboratory fluids.

@item mydrugs:fungal_culture
@item mydrugs:ergot
@item mydrugs:lsd_drop

Use the growth chamber for ergot-related steps, then follow JEI through biochemical and advanced mixing recipes until LSD fluid is produced. To finish the route, drop cupboard pieces on the ground and right-click the dropped stack with a glass bottle containing LSD. Each 5 mB of LSD and one cupboard piece becomes one LSD Drop.

Lysergic Knowledge is granted by consuming the LSD Drop after Stimulant Knowledge is active.

> [GOAL] Unlock Lysergic Knowledge.

---

# Advanced Control Circuits

Lysergic Knowledge unlocks advanced control circuits on the Psy Anvil.

@item mydrugs:advanced_control_circuit
@item mydrugs:reaction_core
@item mydrugs:catalyst_bed
@item mydrugs:packed_column

These parts push the mod into late laboratory machinery: catalytic reforming, cracking, aromatic extraction, and high-tier chemical chains.

> [GOAL] Craft an advanced control circuit.

> [TIP] Advanced circuits are also required for psychotrope components later.

---

# Overclocked Route

The meth route follows Lysergic Knowledge and uses the highest chemical machinery.

@item mydrugs:catalytic_reformer
@item mydrugs:steam_cracker
@item mydrugs:btx_fractionation_tower
@item mydrugs:aromatic_extractor
@item mydrugs:meth_shard
@item mydrugs:meth_powder

Use JEI to build the precursor chain through gases, reactors, reformers, and evaporation. Meth shards are the visible product; grind them into meth powder and use the appropriate smoking route when a consumable form is needed.

> [GOAL] Consume meth through a valid smoking route and unlock Overclocked Knowledge.

> [WARN] Meth is a late route, not a shortcut. Start it only after advanced circuits and chemical handling are stable.

---

# Mushrooms and Mycelial Knowledge

Magic mushrooms are available earlier than their knowledge gate.

@item mydrugs:magic_mushroom
@item mydrugs:magic_mushroom_powder
@item mydrugs:mycelial_resonator

You can find mushrooms in psychedelic terrain and use them as a psychedelic item, but Mycelial Knowledge is only granted after Overclocked Knowledge is active.

Grind mushrooms into powder for late recipes. The mycelial resonator uses mushroom powder, amethyst, redstone, and an advanced control circuit on the Psy Anvil.

> [GOAL] After Overclocked Knowledge, consume a magic mushroom to unlock Mycelial Knowledge.

> [WARN] Eating mushrooms too early does not open the mycelial tree. The knowledge is deferred until the required gate is met.

---

# Psy Mixer Rituals

The Psy Mixer is a multiblock ritual system using the formed Psy Mixer core and parts.

@item mydrugs:painted_clay_bowl
@item mydrugs:psychotropic_pigment
@item mydrugs:ritual_resin
@item mydrugs:brightened_cannabis_powder
@item mydrugs:unstable_residue

Psy Mixer recipes can require knowledge, a specific drug history, a catalyst, a stabilizer, and a vessel. Failure may return unstable residue.

> [TIP] The brightened cannabis powder recipe requires Cannabinoid Knowledge and enough lifetime weed exposure.

> [GOAL] Use JEI's Psy Mixer category before building a ritual setup.

---

# Psychotrope Research

Psychotrope systems are endgame research built from advanced control circuits and high-tier components.

@item mydrugs:psychotrope_lens
@item mydrugs:psychotrope_component
@item mydrugs:psychotrope_core
@item mydrugs:recovery_anchor

Craft the lens first, then components, then the core. The core belongs to a formed multiblock that can generate psychotrope energy.

> [WARN] Psychotrope systems do not block tobacco, cannabis, fermented mash, hash, steel plating, or stimulant progression. They are a late research branch.

> [GOAL] Craft a psychotrope lens after advanced control circuits are available.

---

# Injection and Bottles

Bottles and syringes are delivery tools, not the main unlock path by themselves.

@item mydrugs:glass_bottle
@item mydrugs:syringe

The MyDrugs glass bottle can hold mod fluids and drink fluids marked as drinkable. Syringes are higher-risk delivery items for compatible content.

> [WARN] Injection and high-dose routes raise risk. Keep recovery items ready before experimenting.

> [TIP] If a fluid will not drink from the bottle, it may be a processing fluid rather than a drinkable drug fluid.

---

# Recovery

Addiction and recovery are active systems. Repeated use can build addiction, and withdrawal can create penalties.

@item mydrugs:personal_diary
@item mydrugs:headphones
@item mydrugs:herbal_tea
@item mydrugs:calming_mixture
@item mydrugs:sleeping_aid
@item mydrugs:overdose_antidote
@item mydrugs:therapist_desk
@item mydrugs:recovery_anchor

Recovery items reduce or manage addiction and overdose pressure. Build a plan before late-game stimulant, injection, or overclocked routes.

> [GOAL] Keep at least one recovery option available before repeated drug use.

> [TIP] Recovery is not just flavor. It is part of surviving long production chains.

---

# When You Are Stuck

Check these in order:

## Advancement Tree

The parent advancement usually shows the intended previous gate.

## JEI Category

Search the item, fluid, and machine. Many routes move between several recipe categories.

## Knowledge Gate

Confirm the needed Psy Knowledge is active. A visible recipe can still be locked.

## Inputs and Outputs

Check item slots, fluid tanks, gas tanks, heat, pipe direction, filters, and output space.

## Prototype Content

Some late research systems are visible before every connection is fully final.

> [TIP] The main route through tobacco, cannabis, fermented mash, hash, steel plating, and stimulant should be completable.

---

# Progression Summary

Quick route:

Break grass → grow crops → [[Coffee First|coffee]] → Caffeine Knowledge → Psy Receptacle → strip logs for resin → treated planks → drying rack → grinding bowl → tobacco handful → Nicotinic Knowledge → Psy Anvil → iron mesh → sieve → cured cannabis → dried cannabis + resin → cannabis powder → Cannabinoid Knowledge → copper plates → mixing vat → fermented mash → Fermented Knowledge → heavy iron → stomp crafter → hash → Steel Plating Knowledge → steel plates → machines → coca paste and extracts → cocaine powder → Stimulant Knowledge → insulated wire → control circuits → lysergic lab route → LSD Drop → Lysergic Knowledge → meth route → Overclocked Knowledge → mushroom consumption → Mycelial Knowledge → psychotrope research.

Parallel branches: [[Recovery|recovery]], [[Injection and Bottles|injection]], [[Psy Mixer Rituals|Psy Mixer rituals]], and [[Psychotrope Research|psychotrope energy]].

> [TIP] When in doubt, follow the last knowledge you unlocked. The next locked Psy Anvil or machine recipe usually tells you where to go.
