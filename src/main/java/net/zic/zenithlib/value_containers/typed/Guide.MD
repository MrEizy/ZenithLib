>> This will eventually be merged into a larger doc, but is kept seprate for orginization

A ValueContainer is simply a container for a value(very useful sentence)

This value however, is not directly stored, instead it is calculated from a mass of modifiers.<br>
This is Good for Modding since a lot of the time you will have values that multiple sources might want to modify, and having every
modification be done under an ID and type, it is easier to enforce rules and avoid conflicts

In the case of ValueContainers we have Modifiers.

Modifiers have these properties
- ID:
  - used to identify a modifier
  - a container can only have 1 modifier of an id
- type: 
  - is it a flat or a multiplier
  - a flat can be any type
  - while a multiplier can be any type, value containers only accept double multiplier types
- operation group:
  - determines in which order modifiers are applied.
  - in the same operation group, flat modifiers are always applied first
- group:
  - used to group multiple modifiers in the same operation group together
  - no effect on flat modifiers but does affect multipliers (see [Formula](#formula))




## FORMULA

Before we get into the main formula, I first need to explain Multiplier groups.

A Multiplier is simple, we multiply our value by some other value. but what happens if we have multiplier multipliers?<br>
how should we combine them?

say we have 30% and 50% multipliers, there are 2 main options:
- option 1 add them:
  - 0.3+0.5 = 0.8 or 80% or x1.8
- option 2 multiply them:
  - 1.3*1.5 (we add a one so it does not get smaller) = x1.95 or 95%

<b>How can we specify which operation we want our multiplier modifiers to do?</b>

Multipliers of different operation groups are automatically multiplied together(although there will sometimes be flat modifiers in between)

For Multipliers inside an operation group it can be done through a group, multipliers of the same group will be added together, whilst those not in the same group are multiplied

### Operation groups
There are no limit to the number of operation groups. with the only special operation group being 0 for flat modifiers<br>
this is defined as the BaseValue operation group