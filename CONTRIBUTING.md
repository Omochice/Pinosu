# CONTRIBUTING

This project welcomes contributions.

When raising a problem, we recommend that you first create an issue and then create a PR.

The issue should include the build version where the problem occurred, the steps to reproduce the issue, and the reproduction rate.

## Tech Stack

See [steering files](./.kiro/steering).

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
