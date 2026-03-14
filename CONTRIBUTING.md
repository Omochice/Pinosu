# CONTRIBUTING

This project welcomes contributions.

When raising a problem, we recommend that you first create an issue and then create a PR.

The issue should include the build version where the problem occurred, the steps to reproduce the issue, and the reproduction rate.

## Tech Stack

See [steering files](./doc/steering).

## Setup

This project uses [devbox](https://www.jetify.com/devbox).

The following steps assume that devbox is installed.

### Build

```console
devbox run ./gradlew :app:assembleDebug
```

### Test

```console
devbox run ./gradlew :app:test
```

### Lint

```console
devbox run check
```

### Format

```console
devbox run fmt
```

## Design

Design files are managed under `doc/design.pen`.

When making changes related to UI or design, it is recommended to update the corresponding `.pen` file as well to keep the design and implementation in sync.
