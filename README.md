# Really Simple Tanks
_(A mod for Minecraft)_

This mod adds a tank in which to store fluids. With the Fluid API changes in Forge/Minecraft after 1.13+, I haven't seen very many tanks, much less ones that worked well in the modpacks I've played...so I decided to work on making my own. Currently this mod adds a single block: the Basic Tank. This tank will hold 16 buckets of fluid, which can be inserted or extracted in the GUI via Buckets, or via automation/pipes.
 

Roadmap for the near future:
- [x] Ability to fill/empty the tank by right clicking with a bucket. (v1.01)
- [x] Confirm pipes can fill/extract fluids (or implement that functionality if it isn't working) --(Tested to work with: Xnet, Floppers, and Refined Storage, but needs to put buckets from automation in the correct slots) (v1.0)
- [x] Got automation inputting buckets into the correct slots (fixes above).  All slots will only accept the proper bucket types. (v1.01)
- [ ] Multiple versions of the Tank with larger capacities.
- [ ] Ability to suck or push fluids into adjacent blocks which support that functionality (other tanks or machines with tanks)
- [ ] Ability to void excess fluids and empty the tank.
- [ ] Better block models which render the tank's fluid level externally.


As there aren't many good Fluid "Pipe" mods, the scope of this mod may eventually include a piping system after the core mechanics above are finished.
