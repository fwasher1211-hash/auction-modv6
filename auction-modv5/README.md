# Fartiqs Auction Assistant (Fabric 1.21.11)

Client-side auction setup GUI for Minecraft Java Edition 1.21.11.

## Features
- Right Shift opens the custom GUI.
- Three tabs: item selection, starting/minimum raise, and timer.
- Item search uses the installed game's item registry.
- Start button runs a local countdown and displays the current highest bid.
- Best-effort chat parsing can update the displayed highest bidder when a server chat line matches a generic bid/paid format.

## Important Donut SMP limitation
This is a **client-side helper**, not a server auction integration. It cannot force Donut SMP to create an auction, move a paid bidder to the top, verify payments, or know the server's authoritative bid unless the server announces a recognizable bid/payment line in chat. Chat formats vary, so parsing may need adjustment. It does not automatically submit payments or server commands.

## Build on GitHub (no Gradle installation on your PC)
1. Create a GitHub account if you don't already have one.
2. Create a new empty repository named `fartiqs-auction`.
3. Upload the contents of this project folder to that repository (keep the `.github/workflows/build.yml` path).
4. Open the repository's **Actions** tab.
5. Select **Build Minecraft mod**, then **Run workflow** (or push a commit to trigger it).
6. When the run finishes, open the run and download the artifact named `fartiqs-auction-1.21.11`.
7. Extract the downloaded artifact ZIP and put the `.jar` inside your Minecraft instance's `mods` folder. Install Fabric Loader and Fabric API for Minecraft 1.21.11 too.

The workflow builds with Java 21 and Gradle on GitHub's runners. It does not require Gradle to be installed locally.

## Toolchain
Minecraft 1.21.11, Java 21, Fabric Loader, Fabric API, Yarn mappings, Fabric Loom.
