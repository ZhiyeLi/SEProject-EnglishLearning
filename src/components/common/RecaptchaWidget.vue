<template>
  <div class="recaptcha-wrapper">
    <div ref="recaptchaEl" />
    <p
      v-if="errorMessage"
      class="recaptcha-error"
    >
      {{ errorMessage }}
    </p>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from "vue";

const props = defineProps({
  siteKey: {
    type: String,
    default: "",
  },
  resetSignal: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(["verified", "expired"]);

const recaptchaEl = ref(null);
const widgetId = ref(null);
const errorMessage = ref("");

const scriptUrls = [
  "https://www.google.com/recaptcha/api.js?render=explicit",
  "https://www.recaptcha.net/recaptcha/api.js?render=explicit",
];
const MAX_READY_WAIT_MS = 5000;
const READY_POLL_INTERVAL_MS = 200;

function appendScript(src) {
  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = src;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("reCaptcha 加载失败"));
    document.head.appendChild(script);
  });
}

function loadRecaptchaScript() {
  if (window.grecaptcha) {
    return Promise.resolve();
  }

  if (window.__recaptchaLoading && !window.__recaptchaLoadingFailed) {
    return window.__recaptchaLoading;
  }

  if (window.__recaptchaLoadingFailed) {
    delete window.__recaptchaLoading;
    delete window.__recaptchaLoadingFailed;
  }

  window.__recaptchaLoading = (async () => {
    let lastError = null;
    for (const url of scriptUrls) {
      try {
        await appendScript(url);
        return;
      } catch (error) {
        lastError = error;
      }
    }
    throw lastError || new Error("reCaptcha 加载失败");
  })();

  window.__recaptchaLoading.catch(() => {
    window.__recaptchaLoadingFailed = true;
  });

  return window.__recaptchaLoading;
}

function waitForGrecaptchaReady() {
  return new Promise((resolve, reject) => {
    const start = Date.now();
    const timer = setInterval(() => {
      if (window.grecaptcha && typeof window.grecaptcha.render === "function") {
        clearInterval(timer);
        resolve();
        return;
      }

      if (Date.now() - start >= MAX_READY_WAIT_MS) {
        clearInterval(timer);
        reject(new Error("reCaptcha 初始化失败"));
      }
    }, READY_POLL_INTERVAL_MS);
  });
}

async function renderWidget() {
  if (!recaptchaEl.value || widgetId.value !== null) {
    return;
  }

  if (!props.siteKey) {
    errorMessage.value = "管理员未配置 reCaptcha 站点密钥";
    return;
  }

  try {
    await loadRecaptchaScript();
    await waitForGrecaptchaReady();
    errorMessage.value = "";
    widgetId.value = window.grecaptcha.render(recaptchaEl.value, {
      sitekey: props.siteKey,
      callback: (token) => emit("verified", token),
      "expired-callback": () => emit("expired"),
      "error-callback": () => emit("expired"),
    });
  } catch (error) {
    errorMessage.value = "reCaptcha 初始化失败，请检查密钥、域名或网络";
  }
}

function resetWidget() {
  if (window.grecaptcha && widgetId.value !== null) {
    window.grecaptcha.reset(widgetId.value);
  }
}

onMounted(() => {
  renderWidget();
});

watch(
  () => props.resetSignal,
  () => {
    resetWidget();
  }
);

watch(
  () => props.siteKey,
  () => {
    renderWidget();
  }
);
</script>

<style scoped>
.recaptcha-wrapper {
  margin: 12px 0 4px;
}

.recaptcha-error {
  color: #ef4444;
  font-size: 12px;
  margin-top: 8px;
}
</style>
