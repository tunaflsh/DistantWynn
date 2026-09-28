# DistantWynn

Dynamically adjust the visible region based on the player's location in Wynncraft.

![demo](./demo.png)

## What about WynnVista?

WynnVista dynamically reduces the render distance the moment you step out of the three provinces. There are edge cases this approach won't work. If you are in the Realm of Light, you won't be able to see the other end of the map. And if you're at the south, part of the Gavel province will leak through. This mod is made specifically to address that and the lack of proper voxy support in WynnVista.

Originally, I intended to create PR in WynnVista, but the changes required a full rewrite. So, now it's a standalone mod.

## Features

Use DistantWynn in combination with other mods that enable increased render distance.
- [voxy](https://github.com/MCRcortex/voxy) supported with automatic region detection
- [Sodium](https://github.com/CaffeineMC/sodium) supported
- [Bobby](https://github.com/Johni0702/bobby) support planned
- [Distant Horizons](https://gitlab.com/distant-horizons-team/distant-horizons) support planned

The automatic region detection is a fallback when you're not in one of the following locations:
- Main Wynncraft map (Wynn, Gavel, Fruma provinces)
- Realm of Light

## Roadmap

- Distant Horizons support
- Configure list of predefined locations
- Bobby support

## Acknowledgements

- [WynnVista](https://github.com/DrBiznes/WynnVista) for inspiration
- [voxy](https://github.com/MCRcortex/voxy) for logo inspiration

## License

This project is available under the CC0 license.
