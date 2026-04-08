# Types

## Why this is useful

Scala’s **type system** is how you encode intent, rule out invalid states, and reuse behavior safely. Understanding **ADTs**, **generics**, **variance**, **union and intersection types**, **GADTs**, and **opaque / structural types** lets you model domains precisely and catch mistakes at compile time instead of runtime.

## What’s covered

| File | Topics |
|------|--------|
| `ADTs.scala` | Algebraic data types — sum and product types |
| `DesugaringEnums.scala` | How enums desugar and relate to sealed hierarchies |
| `GADTs.scala` | Generalized algebraic data types |
| `Generics.scala` | Type parameters, bounds, type members |
| `TypeHierarchy.scala` | `Any`, `AnyVal`, `AnyRef`, `Nothing`, top and bottom |
| `Variance.scala` | Covariance, contravariance, invariance |
| `UnionTypes.scala` | Union types (Scala 3) |
| `IntersectionTypes.scala` | Intersection types |
| `opaqueTypes.scala` | Opaque type aliases — newtypes without runtime overhead |
| `StructuralTypes.scala` | Structural typing — duck typing with static checks |
