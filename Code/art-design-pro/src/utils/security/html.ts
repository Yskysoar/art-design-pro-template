import DOMPurify from 'dompurify'

const BLANK_TARGET_SELECTOR = 'a[target="_blank"]'

const addBlankTargetRel = (html: string): string => {
  if (typeof window === 'undefined') return html

  const container = document.createElement('div')
  container.innerHTML = html
  container.querySelectorAll<HTMLAnchorElement>(BLANK_TARGET_SELECTOR).forEach((anchor) => {
    anchor.setAttribute('rel', 'noopener noreferrer')
  })
  return container.innerHTML
}

/**
 * Sanitize rich text produced by the editor before rendering with v-html.
 * Keeps normal article formatting while removing scripts, event handlers and unsafe URLs.
 */
export const sanitizeRichHtml = (html: string): string => {
  const clean = DOMPurify.sanitize(html || '', {
    USE_PROFILES: { html: true },
    ADD_ATTR: ['target', 'rel', 'style', 'class'],
    FORBID_TAGS: ['script', 'iframe', 'object', 'embed', 'form', 'input', 'button'],
    RETURN_TRUSTED_TYPE: false
  })

  return addBlankTargetRel(clean)
}

/**
 * Sanitize small inline HTML snippets, for example scrolling text.
 */
export const sanitizeInlineHtml = (html: string): string => {
  const clean = DOMPurify.sanitize(html || '', {
    ALLOWED_TAGS: ['a', 'strong', 'b', 'em', 'i', 'u', 'span', 'br'],
    ALLOWED_ATTR: ['href', 'target', 'rel', 'class', 'style'],
    RETURN_TRUSTED_TYPE: false
  })

  return addBlankTargetRel(clean)
}

/**
 * Sanitize SVG text before injecting it into the DOM.
 */
export const sanitizeSvg = (svg: string): string => {
  return DOMPurify.sanitize(svg || '', {
    USE_PROFILES: { svg: true, svgFilters: true },
    FORBID_TAGS: ['script', 'foreignObject'],
    RETURN_TRUSTED_TYPE: false
  })
}
