import logging
from typing import List, Tuple, Any

logger = logging.getLogger(__name__)


class RerankerService:
    """Cross-Encoder 重排序服务。

    使用 BAAI/bge-reranker-v2-m3 对初步检索结果进行精细排序。
    模型在首次使用时加载，之后缓存在内存中。
    """

    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._model = None
        return cls._instance

    @property
    def model(self):
        if self._model is None:
            logger.info("Loading BGE reranker model (first use)...")
            from FlagEmbedding import FlagReranker
            self._model = FlagReranker(
                'BAAI/bge-reranker-v2-m3',
                use_fp16=False,
                device="cpu",
            )
            self._patch_tokenizer_prepare_for_model()
            logger.info("BGE reranker model loaded successfully.")
        return self._model

    def _patch_tokenizer_prepare_for_model(self):
        """Monkey-patch prepare_for_model for tokenizers that lack it (transformers>=5.x)."""
        tokenizer = self._model.tokenizer
        if hasattr(tokenizer, "prepare_for_model"):
            return

        def prepare_for_model(
            self_tok, ids, pair_ids=None,
            add_special_tokens=True, truncation=False,
            max_length=None, padding=False, **kwargs
        ):
            # Add special tokens
            if add_special_tokens:
                bos = [self_tok.bos_token_id] if self_tok.bos_token_id is not None else []
                eos = [self_tok.eos_token_id] if self_tok.eos_token_id is not None else []
                if pair_ids is not None:
                    sep = [self_tok.sep_token_id] if self_tok.sep_token_id is not None else []
                    ids = bos + ids + sep
                    pair_ids = pair_ids + eos
                else:
                    ids = bos + ids + eos

            if pair_ids is not None:
                input_ids = ids + pair_ids
            else:
                input_ids = ids

            attention_mask = [1] * len(input_ids)

            # Handle truncation
            if truncation and max_length is not None:
                if len(input_ids) > max_length:
                    input_ids = input_ids[:max_length]
                    attention_mask = attention_mask[:max_length]

            from transformers.tokenization_utils_base import BatchEncoding
            return BatchEncoding({
                "input_ids": input_ids,
                "attention_mask": attention_mask,
            })

        import types
        tokenizer.prepare_for_model = types.MethodType(prepare_for_model, tokenizer)

    def rerank(
        self, query: str, docs_and_scores: List[Tuple[Any, float]], top_k: int = 5
    ) -> List[Tuple[Any, float]]:
        """对候选文档重排序，返回 Top-K。"""
        if not docs_and_scores:
            return []

        pairs = [[query, doc.page_content] for doc, _ in docs_and_scores]
        scores = self.model.compute_score(pairs, normalize=True)

        if isinstance(scores, float):
            scores = [scores]

        reranked = list(zip(
            [doc for doc, _ in docs_and_scores],
            scores,
        ))
        reranked.sort(key=lambda x: x[1], reverse=True)
        return reranked[:top_k]
