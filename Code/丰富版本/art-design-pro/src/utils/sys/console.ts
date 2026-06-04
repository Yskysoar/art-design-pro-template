const asciiArt = `
\x1b[32mAdmin Template ready.
\x1b[0m
\x1b[36mUse local Mock for development, then switch to real backend APIs before production.
\x1b[0m
`

if (import.meta.env.DEV) {
  console.log(asciiArt)
}
