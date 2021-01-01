# Templates of buildable structures
Template file:

```
# This is a comment
# <symbol>,block_name,prop1:val1,prop2:val2
_, # Skip block
A,air # Remove block, leave air
W,oak_stairs,half:bottom,facing:west # Oak stairs with base on the bottom and facing west.

# <width>x<height>x<depth>
5x3x3

# South-most face, left = west, right = east
_ _ _ _ _
_ A W A _
_ _ _ _ _

# Middle face, left = west, right = east
_ _ _ _ _
_ W A W _
_ _ _ _ _

# North-most face, left = west, right = east
_ _ W _ _
W A A A W
_ _ W _ _
```
