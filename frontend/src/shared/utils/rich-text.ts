const allowedTags = new Set([
  "B",
  "BLOCKQUOTE",
  "BR",
  "CODE",
  "EM",
  "H2",
  "H3",
  "HR",
  "I",
  "LI",
  "OL",
  "P",
  "PRE",
  "S",
  "STRONG",
  "UL",
]);
const blockedTags = new Set(["IFRAME", "OBJECT", "SCRIPT", "STYLE", "EMBED", "LINK", "META"]);

export function sanitizeRichTextHtml(html: string): string {
  if (!html) {
    return "";
  }

  if (typeof DOMParser === "undefined" || typeof document === "undefined") {
    return escapeHtml(html);
  }

  const parsed = new DOMParser().parseFromString(html, "text/html");
  cleanChildren(parsed.body);
  return parsed.body.innerHTML;
}

function cleanChildren(parent: Node) {
  Array.from(parent.childNodes).forEach((child) => cleanNode(child));
}

function cleanNode(node: Node) {
  if (node.nodeType === Node.COMMENT_NODE) {
    node.parentNode?.removeChild(node);
    return;
  }

  if (node.nodeType !== Node.ELEMENT_NODE) {
    return;
  }

  const element = node as HTMLElement;
  cleanChildren(element);

  if (blockedTags.has(element.tagName)) {
    element.remove();
    return;
  }

  if (!allowedTags.has(element.tagName)) {
    unwrapElement(element);
    return;
  }

  Array.from(element.attributes).forEach((attribute) => {
    element.removeAttribute(attribute.name);
  });
}

function unwrapElement(element: HTMLElement) {
  const parent = element.parentNode;
  if (!parent) {
    return;
  }

  while (element.firstChild) {
    parent.insertBefore(element.firstChild, element);
  }
  parent.removeChild(element);
}

function escapeHtml(value: string): string {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#39;");
}
