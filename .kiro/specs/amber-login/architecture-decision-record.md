# Amber Login - Architecture Decision Record

## Feature Goals

This feature is implemented as the authentication foundation for the Nostr bookmark application Pinosu as follows:

- Secure Login: Provides secure login functionality without holding private keys through NIP-55 protocol integration with the Amber app
- Login State Persistence: Safely stores login state in local storage to maintain user experience after app restarts
- Guidance for Missing Amber: Provides user guidance (directing to Google Play Store) when the Amber app is not installed
- Android Standard Security Compliance: Security implementation that leverages Android Keystore and EncryptedSharedPreferences and conforms to best practices

## Feature Non-Goals

The following items are intentionally not implemented in this feature:

- Relay Server Connection: Planned for future phases
- Bookmark Feature (kind 10003): Only login functionality in this phase
- Multiple Account Management: Supports single user only
- iOS Support: Android-only
- Direct NIP-46 Relay Communication Implementation: Not needed due to NIP-55 usage
- Offline Authentication: Requires communication with Amber

---

## Selected Technologies

### 1. NIP-55 Protocol (Amber Communication)

#### Pros

- Official Support: Amber officially supports NIP-55 with proven track record
- Android Standard Intent Mechanism: High reliability due to using Android standard Intent mechanism with good platform compatibility
- Built-in User Approval Flow: User approval flow is standardized, providing secure UX
- Wide Adoption: Widely adopted by other Nostr Android apps including Amethyst, 0xChat, Voyage, and Fountain
- Security: Private keys are managed by Amber, minimizing security risks on the Pinosu app side

#### Cons

- Android-only: Alternative implementation methods required for iOS (no impact in this Android-limited project)
- Amber Installation Required: Login not possible if user doesn't install Amber app (mitigated by guidance)
- External Dependency: Depends on Amber app maintenance status

### 2. Amethyst Quartz (Nostr Library)

#### Pros

- Amber Integration Support: `NostrSignerExternal` class directly supports Amber integration
- Battle-tested: Actually operated in Amethyst with high reliability
- Future Extensibility: Relay communication and other NIP implementations can be easily added
- Maven Central Availability: Easy dependency management and standard build process integration
- Implementation Time Reduction: No need to implement protocol from scratch, improving development efficiency

#### Cons

- External Dependency: Depends on library version compatibility and maintenance status
- Learning Cost: Need to understand library API specifications
- Library Size: Potential app size increase

### 3. Clean Architecture + MVVM + Jetpack Compose

#### Pros

- Google Official Recommendation: Standard architectural pattern for Android development
- Jetpack Compose Compatibility: Best compatibility with modern UI development framework
- Testability: ViewModel unit testing is easy, layer separation clarifies dependency management
- Maintainability and Extensibility: Separation of concerns enables easy future feature expansion and high code maintainability
- State Management Clarity: Login state management is clarified in ViewModel, separating UI layer from business logic

#### Cons

- Initial Implementation Complexity: Layer separation increases boilerplate code
- Learning Cost: Requires understanding Clean Architecture concepts
- Overkill for Small Apps: Simple login-only functionality might suffice with simpler patterns

### 4. Android Keystore + EncryptedSharedPreferences

#### Pros

- Android Jetpack Security Standard: High reliability as official Android security library
- Automatic Encryption: Data automatically encrypted with AES256
- Hardware Protection: Master key protected by Android Keystore, utilizing TEE/SE
- Implementation Simplicity: Possible to implement without conscious effort on complex encryption
- Security Best Practices: Compliant with Android security standards as of 2024

#### Cons

- Android 6.0+ (API 23+) Required: Not usable on older devices (no impact in this project with minSdk 26)
- Key Material Backup Not Possible: Login state lost during device migration (re-authentication required)
- Encryption Overhead: Possible slight performance overhead

### 5. ActivityResultAPI

#### Pros

- Modern API: Recommended alternative to deprecated `startActivityForResult()`
- Android 13+ Support: Guaranteed operation on latest Android versions
- Type-Safe: Contract-based API enables type-safe implementation
- Lifecycle Management: Automatically integrated with Android lifecycle, preventing memory leaks
- Jetpack Compose Integration: Seamless integration with Compose

#### Cons

- Learning Cost: Need to understand new API paradigm
- Migration from Legacy: May require migration from legacy code

---

## Evaluated Alternative Technologies

### Alternative 1: Direct NIP-46 Implementation (Relay-based Communication)

#### Pros

- Platform Independence: Same implementation usable on platforms other than Android
- Serverless: Complete decentralized architecture
- No External App: Amber app installation not required

#### Cons

- Implementation Complexity: Complex implementation needed for relay server communication, NIP-44 encryption, JSON-RPC implementation
- Network Dependency: Network connection to relay server essential
- Latency: Response time longer due to relay-based communication
- Security Risk: Private key management becomes more complex

### Alternative 2: nostr-java-library

#### Pros

- JVM Standard: Java-based library, works in standard JVM environment
- Alternative Option: Serves as fallback if Quartz has issues

#### Cons

- Amber Integration Support Unclear: Unclear if `NostrSignerExternal` equivalent functionality exists
- Insufficient Track Record: Less battle-tested than Quartz
- Documentation: Limited documentation and community support compared to Quartz

### Alternative 3: Custom Nostr Protocol Implementation

#### Pros

- Complete Control: Full control of protocol implementation
- No External Dependency: Not affected by library maintenance
- Optimization: Project-specific optimization possible

#### Cons

- Implementation Time: Requires enormous time and resources for protocol implementation
- Testing and Debugging: Difficult to verify protocol compliance
- Maintenance Cost: Need to address NIP specification changes
- Bug Risk: Security risks from implementation bugs

### Alternative 4: MVC (Model-View-Controller)

#### Pros

- Simple: Simple implementation, easy to understand
- Low Learning Cost: Low learning curve with traditional pattern

#### Cons

- Complex State Management: UI state management tends to become complex
- Jetpack Compose Compatibility: Poor compatibility with modern Android development
- Testability: View and Controller tend to be tightly coupled, making testing difficult
- Deprecated: Treated as legacy approach

### Alternative 5: Plaintext SharedPreferences

#### Pros

- Simple: Simplest implementation
- Performance: No encryption overhead

#### Cons

- Security Risk: Data stored in plaintext, easily readable via rooted devices or ADB access
- Compliance: Violates security best practices
- Audit: Likely to be pointed out in security audits

### Alternative 6: Room + SQLCipher

#### Pros

- Strong Encryption: Entire database encrypted
- Future Extensibility: Can handle complex data models

#### Cons

- Over-engineering: Excessive for simple key-value storage
- Implementation Complexity: Need for database schema definition, migration management
- Performance Overhead: SQLite overhead incurred
- Increased Dependencies: Additional library required

### Alternative 7: startActivityForResult()

#### Pros

- Legacy Track Record: Widely used in older Android versions
- Simple: Traditional implementation pattern, easy to understand

#### Cons

- Deprecated: Deprecated by Android official
- Android 13+ Support: Possible operation not guaranteed on latest Android versions
- Lifecycle Management: Risk of memory leaks
- Jetpack Compose Integration: Memory leak risk in Compose integration

---

## Summary

This feature implements the selected NIP-55 protocol, Amethyst Quartz, Clean Architecture + MVVM + Jetpack Compose, Android Keystore + EncryptedSharedPreferences, and ActivityResultAPI to realize secure and maintainable login functionality.

These technology selections comply with Android standard best practices and provide the foundation for future feature expansion (relay communication, bookmark functionality). Compared to alternatives, these selections offer the best balance of implementation time, security, and maintainability.
