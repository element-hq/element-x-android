This file contains some rough notes about Oidc implementation, with some examples of actual data.

[ios implementation](https://github.com/element-hq/element-x-ios/compare/develop...doug/oidc-temp)

Rust sdk branch: https://github.com/matrix-org/matrix-rust-sdk/tree/oidc-ffi

Figma https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?node-id=133-5426&t=yQXKeANatk6keoZF-0

Server list: https://github.com/element-hq/oidc-playground

Metadata iOS: (from https://github.com/element-hq/element-x-ios/blob/5f9d07377cebc4f21d9668b1a25f6e3bb22f64a1/ElementX/Sources/Services/Authentication/AuthenticationServiceProxy.swift#L28)

clientName: InfoPlistReader.main.bundleDisplayName,
redirectUri: "io.element.android:/",
clientUri: "https://element.io",
tosUri: "https://element.io/user-terms-of-service",
policyUri: "https://element.io/privacy"


Android:
clientName = "Element",
redirectUri = "io.element.android:/",
clientUri = "https://element.io",
tosUri = "https://element.io/user-terms-of-service",
policyUri = "https://element.io/privacy"


Example of OidcData (from presentUrl callback):
url: https://auth-oidc.lab.element.dev/authorize?response_type=code&client_id=01GYCAGG3PA70CJ97ZVP0WFJY3&redirect_uri=io.element%3A%2Fcallback&scope=openid+urn%3Amatrix%3Aorg.matrix.msc2967.client%3Aapi%3A*+urn%3Amatrix%3Aorg.matrix.msc2967.client%3Adevice%3AYAgcPW4mcG&state=ex6mNJVFZ5jn9wL8&nonce=NZ93DOyIGQd9exPQ&code_challenge_method=S256&code_challenge=FFRcPALNSPCh-ZgpyTRFu_h8NZJVncfvihbfT9CyX8U&prompt=consent

Formatted url:
https://auth-oidc.lab.element.dev/authorize?
    response_type=code&
    client_id=01GYCAGG3PA70CJ97ZVP0WFJY3&
    redirect_uri=io.element%3A%2Fcallback&
    scope=openid+urn%3Amatrix%3Aorg.matrix.msc2967.client%3Aapi%3A*+urn%3Amatrix%3Aorg.matrix.msc2967.client%3Adevice%3AYAgcPW4mcG&
    state=ex6mNJVFZ5jn9wL8&
    nonce=NZ93DOyIGQd9exPQ&
    code_challenge_method=S256&
    code_challenge=FFRcPALNSPCh-ZgpyTRFu_h8NZJVncfvihbfT9CyX8U&
    prompt=consent

state: ex6mNJVFZ5jn9wL8


Oidc client example: https://github.com/matrix-org/matrix-rust-sdk/blob/39ad8a46801fb4317a777ebf895822b3675b709c/examples/oidc_cli/src/main.rs
Oidc sdk doc: https://github.com/matrix-org/matrix-rust-sdk/blob/39ad8a46801fb4317a777ebf895822b3675b709c/crates/matrix-sdk/src/oidc.rs


Test server:
synapse-oidc.lab.element.dev
