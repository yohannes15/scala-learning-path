# Capstone notes — `MiniCli` input parsing

## Regex `[,\\s]+` (Scala string passed to `split`)

- Splits on **one or more** commas and/or **whitespace** (space, tab, …).
- Examples that yield **two tokens**: `"720 50000"`, `"720,50000"`, `"720 , 50000"`.

### Pieces

| Piece | Meaning |
| --- | --- |
| `[` … `]` | One character from the set inside. |
| `,` | Literal comma. |
| `\\s` in a Scala string | Becomes `\s` in the regex → whitespace. |
| `+` | One or more of the preceding group (here, the character class). |
| `\\` in the string | Escaping so the compiler passes `\` through to the regex engine. |

## Empty lines

- After `trim`, an empty line is `""`.
- `"".split("[,\\s]+")` yields **`Array("")`**: length **1**, not 0 — one **empty** token. That is why a generic “wrong number of fields” message felt wrong for “user typed nothing.”
- Handle **`line.isEmpty`** before `split` if you want a clear “no input” error.
