import MarkdownIt from "markdown-it";
import createDOMPurify from "dompurify";

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: true,
});

const getDOMPurify = () => {
  if (typeof window === "undefined") return null;

  // dompurify 在不同打包环境下可能导出“实例”或“工厂函数”
  // - 若是工厂：createDOMPurify(window) -> DOMPurify
  // - 若是实例：createDOMPurify.sanitize 直接可用
  if (typeof createDOMPurify === "function" && !createDOMPurify.sanitize) {
    return createDOMPurify(window);
  }

  return createDOMPurify;
};

export const renderMarkdownToSafeHtml = (markdownText) => {
  const raw = md.render(markdownText || "");
  const purify = getDOMPurify();
  if (!purify) return raw;

  return purify.sanitize(raw, {
    USE_PROFILES: { html: true },
  });
};
