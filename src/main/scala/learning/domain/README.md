# Domain modeling

## Why this is useful

Real programs need a **model of the problem domain**: entities, behaviors, and data. Scala supports both **object-oriented** decomposition (traits, classes, mixin composition) and **functional** modeling (case classes, enums, pure operations separated from data). This folder contrasts and combines those styles.

## What’s covered

| File | Topics |
|------|--------|
| `DomainModeling.scala` | Traits, classes, enums (`Color`, `Planet`, etc.), case classes, pizza-ordering-style examples |
| `OopModeling.scala` | OOP patterns in Scala: traits as interfaces, mixin stacks |
| `OopModelingExample.scala` | Advanced OOP: abstract type members, self-types, subject–observer style |
| `FpModeling.scala` | FP-oriented modeling: enums, case classes, operations as separate functions |

Other modules (for example **`fundamentals`**) may **import** types declared here so examples stay realistic across package boundaries.
