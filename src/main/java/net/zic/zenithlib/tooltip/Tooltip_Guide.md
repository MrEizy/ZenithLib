# ZenithLib Tooltips

This file explains the basic parts of the ZenithLib tooltip system.

## 1. What the tooltip system does

ZenithLib lets mods and resource packs replace normal Minecraft item tooltips with nicer custom tooltips.

A tooltip can have:

- a title
- an item icon
- normal text
- rows of information
- badges
- dividers
- bars
- multiple pages
- scrolling
- conditional sections
- dynamic values
- item classifications
- animations

Most simple tooltips can be made with JSON files.
More advanced tooltips can use Java providers for live data.

## 2. The four main file types

A ZenithLib tooltip normally uses four types of files:

1. Definition - which item, tag, or mod namespace gets the tooltip.
2. Template - what the tooltip contains.
3. Theme - how the tooltip looks.
4. Animation preset - how the tooltip moves or animates.

## 3. Resource folders

Tooltip files go inside the mod or resource pack assets folder.

Use these folders:

```text
assets/<namespace>/zenith_tooltips/definitions/
assets/<namespace>/zenith_tooltips/templates/
assets/<namespace>/zenith_tooltips/themes/
assets/<namespace>/zenith_tooltips/animation_presets/
```

E.g. for Ascension:

```text
assets/ascension/zenith_tooltips/definitions/
assets/ascension/zenith_tooltips/templates/
assets/ascension/zenith_tooltips/themes/
assets/ascension/zenith_tooltips/animation_presets/
```

## 4. Definitions

A definition tells ZenithLib which item or group of items should use a tooltip.
A definition can target exact items, item tags, all items from a namespace/mod id
A definition usually points to a template and a theme.

Basic item definition:

```json
{
  "priority": 100,
  "selector": {
    "items": [
      "minecraft:diamond"
    ]
  },
  "template": "zenithlib:showcase_material",
  "theme": "zenithlib:mana_blue"
}
```

What this means:

- This applies to minecraft:diamond.
- It uses the template zenithlib:showcase_material.
- It uses the theme zenithlib:mana_blue.
- Priority decides which definition wins if more than one matches.

Higher priority wins.

### Tag definitions

A tag definition applies to every item inside an item tag.

Example:

```json
{
  "priority": 50,
  "selector": {
    "tags": [
      "c:ingots/iron"
    ]
  },
  "template": "example:material",
  "theme": "example:metal"
}
```

What this means; Every item in the tag `c:ingots/iron` uses this tooltip.

### Namespace definitions

A namespace definition applies to every item from a mod id.

Example:

```json
{
  "priority": -100,
  "selector": {
    "namespaces": [
      "ascension"
    ]
  },
  "template": "ascension:generic_item",
  "theme": "ascension:default"
}
```

What this means; Every item from the namespace gets this tooltip unless a higher-priority rule overrides it.

### Vanilla converted fallback

What this means:
- Items without custom JSON can still use the ZenithLib tooltip renderer.
- The normal item name and tooltip lines are reused.
- Enchantments, stats, durability, and normal vanilla tooltip text can be shown with a cleaner ZenithLib layout.
- This is useful because not every item needs a handwritten tooltip.


## 5. Templates

A template describes what appears inside the tooltip. Templates can be reused by many items.

Example template:

```json
{
  "pages": [
    {
      "title": {
        "source": "zenithlib:item_name"
      },
      "elements": [
        {
          "type": "zenithlib:title_icon",
          "title": {
            "source": "zenithlib:item_name"
          },
          "subtitle": {
            "literal": "Material"
          }
        },
        {
          "type": "zenithlib:row",
          "left": {
            "literal": "Item ID"
          },
          "right": {
            "source": "zenithlib:item_id"
          }
        },
        {
          "type": "zenithlib:divider"
        },
        {
          "type": "zenithlib:text",
          "text": {
            "literal": "A simple custom tooltip."
          }
        }
      ]
    }
  ]
}
```

What this means:

- The tooltip has one page.
- The title uses the current item's name.
- The title icon section shows the item name and a subtitle.
- The row shows the item id.
- The divider separates sections.
- The text element shows a normal sentence.

### Pages

Templates can have one page or many pages.

A page has:

- a title
- a list of elements

Example with two pages:

```json
{
  "pages": [
    {
      "title": {
        "literal": "Overview"
      },
      "elements": [
        {
          "type": "zenithlib:text",
          "text": {
            "literal": "This is the first page."
          }
        }
      ]
    },
    {
      "title": {
        "literal": "Details"
      },
      "elements": [
        {
          "type": "zenithlib:text",
          "text": {
            "literal": "This is the second page."
          }
        }
      ]
    }
  ]
}
```

What this means:

- The tooltip has two pages.
- The player can switch between them using the tooltip navigation controls.

## 6. Common element types

Elements are the pieces that make up a tooltip.

Common elements:

- **zenithlib:title_icon**  
  Shows a main title, subtitle, and item/icon area.

- **zenithlib:text**  
  Shows normal text.

- **zenithlib:header**  
  Shows section header text.

- **zenithlib:row**  
  Shows a left value and a right value.  
  Can also include an icon.

- **zenithlib:badge**  
  Shows a compact label, often used for category, rarity, type, or status.

- **zenithlib:divider**  
  Shows a line between sections.

- **zenithlib:spacer**  
  Adds empty space.

- **zenithlib:bar**  
  Shows a progress bar or gauge.

- **zenithlib:section**  
  Shows a group of elements, optionally only when a condition is true.

- **zenithlib:classification**  
  Shows category and/or rank information for the item.

- **zenithlib:dynamic**  
  Asks Java for a list of elements at runtime.

### Text values

Text fields usually use literal text, translatable text, or a source.

Use `literal` for quick tests or simple text that will never need translation.  
Use `translatable` for normal player-facing text. This is usually the best choice for real mod tooltips because it lets the text go in a lang file.
Use `source` for live values, such as the item name, item id, durability, purity, or other data from Java.  

Literal text:

```json
{
  "literal": "Purity"
}
```
What this means:
- Always displays the word Purity.

Translatable text:

```json
{
  "translatable": "tooltip.example.material.badge"
}
```
What this means:
- Always whatever the key says.


Dynamic source text:

```json
{
  "source": "zenithlib:item_name"
}
```
What this means:
- Asks ZenithLib for the current item's name.


Common built-in sources:

- **zenithlib:item_name**  
  The current item's display name.

- **zenithlib:item_id**  
  The current item's registry id.

- **zenithlib:durability**  
  The current item's durability, if it has durability.

Mods can register their own sources in Java.

### Row example

Rows are good for small pieces of information.

Example:

```json
{
  "type": "zenithlib:row",
  "left": {
    "literal": "Item ID"
  },
  "right": {
    "source": "zenithlib:item_id"
  }
}
```

What this means:

- Left side says Item ID.
- Right side shows the actual item id.

### Row with icon example

```json
{
  "type": "zenithlib:row",
  "icon": {
    "texture": "ascension:textures/gui/tooltips/stat/purity.png"
  },
  "left": {
    "literal": "Purity"
  },
  "right": {
    "source": "ascension:bloodline_purity"
  }
}
```

What this means:

- Shows a small icon.
- Shows Purity on the left.
- Shows a dynamic purity value on the right.

### Badge example

```json
{
  "type": "zenithlib:badge",
  "text": {
    "literal": "Rare"
  }
}
```

What this means:

- Shows a compact Rare badge.

Badges are useful for:

- rarity
- type
- category
- status
- required key hints

### Bar examples

Bars are used for progress-like values.

Simple durability bar:

```json
{
  "type": "zenithlib:bar",
  "label": {
    "literal": "Durability"
  },
  "source": "zenithlib:durability"
}
```

What this means:

- Shows durability if the item has durability.

Custom value/max bar:

```json
{
  "type": "zenithlib:bar",
  "label": {
    "literal": "Purity"
  },
  "value": {
    "source": "ascension:bloodline_purity_value"
  },
  "max": {
    "source": "ascension:bloodline_purity_max"
  }
}
```

What this means:

- The current value comes from ascension:bloodline_purity_value.
- The max value comes from ascension:bloodline_purity_max.
- Java provides those numbers.

### Conditional sections

A conditional section only appears when a condition is true.

Example Shift section:

```json
{
  "type": "zenithlib:section",
  "condition": "zenithlib:shift_down",
  "elements": [
    {
      "type": "zenithlib:text",
      "text": {
        "literal": "Extra details shown while Shift is held."
      }
    }
  ]
}
```

What this means:

- This text only appears when the player holds Shift.

Built-in conditions:

- zenithlib:shift_down
- zenithlib:ctrl_down
- zenithlib:alt_down
- zenithlib:not_shift_down
- zenithlib:not_ctrl_down
- zenithlib:not_alt_down

Example hint section:

```json
{
  "type": "zenithlib:section",
  "condition": "zenithlib:not_shift_down",
  "elements": [
    {
      "type": "zenithlib:text",
      "text": {
        "literal": "Hold Shift for more details."
      }
    }
  ]
}
```

What this means:

- This text appears only while Shift is not held.

### Classification

Classification is used for item category and rank.

Example categories:

- artifact
- essence
- technique
- material
- consumable

Example ranks:

- common
- rare
- divine
- mythic

Classification element example:

```json
{
  "type": "zenithlib:classification",
  "show_category": true,
  "show_rank": true,
  "style": "badge"
}
```

What this means:

- Shows both category and rank.
- Uses badge style.

Category only:

```json
{
  "type": "zenithlib:classification",
  "show_category": true,
  "show_rank": false,
  "style": "row"
}
```

Rank only:

```json
{
  "type": "zenithlib:classification",
  "show_category": false,
  "show_rank": true,
  "style": "row"
}
```

What this means:

- A mod can provide category/rank data in Java.
- The template decides how much of that data is shown.

## 7. Themes

A theme controls the visual style of the tooltip.

Themes can control things like:

- background color
- border colors
- text colors
- padding
- max width
- spacing
- frame style

Example theme:

```json
{
  "background": "#101522EE",
  "border_top": "#7FB8FFFF",
  "border_bottom": "#264E89FF",
  "text": "#EAF2FFFF",
  "muted_text": "#9FB2CCFF",
  "accent": "#66CCFFFF",
  "padding": 7,
  "max_width": 260
}
```

What this means:

- The tooltip has a dark blue background.
- The border fades from light blue to darker blue.
- Text is pale blue-white.
- Padding and max width control the tooltip size.

Exact fields may depend on the current ZenithLib theme codec.
Use generated showcase themes as examples.

## 8. Animation presets

An animation preset is a reusable animation bundle.

A template can reference one or more animation presets.

Example template using a preset:

```json
{
  "animation_presets": [
    "zenithlib:celestial"
  ],
  "pages": [
    {
      "title": {
        "source": "zenithlib:item_name"
      },
      "elements": [
        {
          "type": "zenithlib:text",
          "text": {
            "literal": "Animated tooltip example."
          }
        }
      ]
    }
  ]
}
```

Animation presets may affect:

- text
- background
- border
- dividers
- bars
- icons
- page/open motion

Use animation presets when many tooltips should share the same movement style.

Animation preset files go here: `assets/<namespace>/zenith_tooltips/animation_presets/`

They could be something like this:
```json
{
  "parents": [
    "zenithlib:celestial"
  ],
  "effects": [
    "divider_sweep",
    "bar_edge_sparks"
  ]
}
```
What this means:
- This preset starts with the built-in zenithlib:celestial preset.
- It adds a divider sweep effect.
- It also adds sparks at the edge of bars.


## 9. Text effects

Text-bearing elements can use text effects if the element supports them.

Text-bearing elements include:

- text
- header
- title_icon title/subtitle
- row left/right text
- badge text
- bar label text

Effects may include things like:

- shimmer
- gradient
- rainbow
- wave
- typewriter
- rune decipher
- scramble/reveal

Use text effects sparingly.
Too many effects in one tooltip can make it hard to read.

## 10. Dynamic Java sources

Some values should come from Java instead of fixed JSON.

Use Java sources when the tooltip needs information from:

- the item stack
- item components
- player data
- registries
- mod-specific systems

Example text source registration:  
The Java examples below are simple pseudocode examples and should not be used directly.

```text
ZenithTooltipSources.registerText(
Identifier.fromNamespaceAndPath("ascension", "bloodline_name"),
context -> Optional.of(Component.literal("Azure Dragon"))
);
```

Example number source registration:

```text
ZenithTooltipSources.registerNumber(
Identifier.fromNamespaceAndPath("ascension", "bloodline_purity_value"),
context -> Optional.of(75)
);
```

```text
ZenithTooltipSources.registerNumber(
Identifier.fromNamespaceAndPath("ascension", "bloodline_purity_max"),
context -> Optional.of(100)
);
```

What this means:

- JSON asks for ascension:bloodline_purity_value.
- Java returns the actual number.
- The tooltip uses that number in a row, bar, or other element.

## 11. Custom conditions in Java

Mods can register their own conditions.

Example:

```text
ZenithTooltipConditions.register(
Identifier.fromNamespaceAndPath("ascension", "has_bloodline"),
context -> context.stack().has(AscensionComponents.BLOODLINE)
);
```

What this means:

- JSON can use condition ascension:has_bloodline.
- The section appears only when the item stack has the bloodline component.

JSON example:

```json
{
  "type": "zenithlib:section",
  "condition": "ascension:has_bloodline",
  "elements": [
    {
      "type": "zenithlib:text",
      "text": {
        "literal": "This item has bloodline data."
      }
    }
  ]
}
```

## 12. Dynamic element lists

A dynamic element asks Java to create one or more tooltip elements at runtime.

Example JSON:

```json
{
  "type": "zenithlib:dynamic",
  "source": "ascension:technique_stats"
}
```

What this means:

- ZenithLib asks Java for the elements from ascension:technique_stats.
- Java returns the rows, badges, or text that should appear.

Use dynamic elements when the tooltip content is too flexible for normal JSON.
Do not use dynamic elements when a simple source value is enough.

## 13. Code-driven tooltip providers

A tooltip provider can create or modify tooltips in Java.

Use a provider when:

- JSON is not enough
- tooltip contents are highly custom
- tooltip data depends on complex stack data
- another mod wants to integrate with ZenithLib directly

For most item families, JSON templates plus Java sources are easier.

## 14. Data-Gen helpers

Data-Gen can produce tooltip JSON automatically.

Useful helper ideas:

- itemTooltip
- tagTooltip
- namespaceTooltip
- template
- theme
- animationPreset

Example item tooltip data-gen idea:

```text
itemTooltip(
"tablet_of_destruction",
external("ascension", "tablet_of_destruction"),
external("ascension", "destruction_tablet")
).priority(200).theme(external("ascension", "destruction"));
```

What this means:

- Generate a definition file for ascension:tablet_of_destruction.
- It uses template ascension:destruction_tablet.
- It uses theme ascension:destruction.
- It has priority 200.

Example tag tooltip data-gen idea:

```text
tagTooltip(
"bloodline_essences",
external("ascension", "bloodline_essences"),
external("ascension", "bloodline_essence")
).priority(100).theme(external("ascension", "blood"));
```

What this means:

- Generate a definition file for all items in the ascension:bloodline_essences tag.
- They all use the same bloodline essence template.

## 15. A complete Tooltip Example

#### Definition:
Goes in; `assets/example/zenith_tooltips/definitions/diamond.json`  
Contents;
```json
{
  "priority": 100,
  "selector": {
    "items": [
      "minecraft:diamond"
    ]
  },
  "template": "example:simple_diamond",
  "theme": "example:blue"
}
```

#### Template:
Goes in; `assets/example/zenith_tooltips/templates/simple_diamond.json`  
Contents;
```json
{
  "pages": [
    {
      "title": {
        "source": "zenithlib:item_name"
      },
      "elements": [
        {
          "type": "zenithlib:title_icon",
          "title": {
            "source": "zenithlib:item_name"
          },
          "subtitle": {
            "translatable": "tooltip.example.material.subtitle"
          }
        },
        {
          "type": "zenithlib:badge",
          "text": {
            "translatable": "tooltip.example.material.badge"
          }
        },
        {
          "type": "zenithlib:row",
          "left": {
            "translatable": "tooltip.example.item_id"
          },
          "right": {
            "source": "zenithlib:item_id"
          }
        },
        {
          "type": "zenithlib:divider"
        },
        {
          "type": "zenithlib:text",
          "text": {
            "translatable": "tooltip.example.material.description"
          }
        },
        {
          "type": "zenithlib:section",
          "condition": "zenithlib:not_shift_down",
          "elements": [
            {
              "type": "zenithlib:text",
              "text": {
                "literal": "Hold Shift for more details."
              }
            }
          ]
        },
        {
          "type": "zenithlib:section",
          "condition": "zenithlib:shift_down",
          "elements": [
            {
              "type": "zenithlib:text",
              "text": {
                "literal": "Diamonds can be used for tools, armor, enchanting tables, and other recipes."
              }
            }
          ]
        }
      ]
    }
  ]
}
```
And make sure these are in your lang file;
```
"tooltip.example.material.subtitle": "Example Material",
"tooltip.example.material.badge": "Material",
"tooltip.example.item_id": "Item ID",
"tooltip.example.material.description": "A simple tooltip made with ZenithLib.",
```

What does this mean?:
- Shows the current item name.
- Shows a subtitle.
- Shows a small Material badge.
- Shows the item id using `zenithlib:item_id`.
- Shows normal text.
- Shows a short hint when Shift is not held.
- Shows extra information when Shift is held.

#### Theme:
Goes in; `assets/example/zenith_tooltips/themes/blue.json`  
Contents;
```json
{
  "background": "#101522EE",
  "border_top": "#7FB8FFFF",
  "border_bottom": "#264E89FF",
  "text": "#EAF2FFFF",
  "muted_text": "#9FB2CCFF",
  "accent": "#66CCFFFF",
  "padding": 7,
  "max_width": 260
}
```

What does this mean?:
- Gives the tooltip a dark blue background.
- Gives it a blue border.
- Sets normal and muted text colors.
- Sets the tooltip padding and width.