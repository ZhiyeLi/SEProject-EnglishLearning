# rag_service/tests/test_generate.py
import pytest
from unittest.mock import MagicMock
from graph.nodes.generate import format_docs, build_generate_messages


class TestFormatDocs:
    def test_format_empty_docs(self):
        assert "无匹配内容" in format_docs([])

    def test_format_single_doc_with_level(self):
        from langchain_core.documents import Document
        doc = Document(page_content="abandon 放弃", metadata={"level": "四级词汇"})
        result = format_docs([doc])
        assert "abandon 放弃" in result
        assert "[收录等级: 四级词汇]" in result

    def test_format_multiple_docs_separated(self):
        from langchain_core.documents import Document
        docs = [
            Document(page_content="doc1", metadata={"level": "A"}),
            Document(page_content="doc2", metadata={"level": "B"}),
        ]
        result = format_docs(docs)
        assert "---" in result

    def test_format_doc_without_level(self):
        from langchain_core.documents import Document
        doc = Document(page_content="no level doc", metadata={})
        result = format_docs([doc])
        assert "[收录等级" not in result


class TestBuildGenerateMessages:
    def _make_state(self, **overrides):
        state = {
            "query": "test query",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "relevant",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        state.update(overrides)
        return state

    def test_build_messages_with_docs(self):
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="abandon 放弃", metadata={"level": "四级词汇"})
        ])
        messages = build_generate_messages(state)
        assert len(messages) == 2
        assert messages[0].type == "system"
        assert "abandon" in messages[0].content

    def test_build_messages_without_docs(self):
        state = self._make_state(retrieved_docs=[])
        messages = build_generate_messages(state)
        assert "我暂时未在平台资料中找到" in messages[0].content

    def test_build_messages_includes_history(self):
        state = self._make_state(chat_history=[
            {"role": "user", "content": "hello"},
            {"role": "assistant", "content": "hi"},
        ])
        messages = build_generate_messages(state)
        assert "hello" in messages[0].content

    def test_build_messages_user_level(self):
        state = self._make_state(user_level="advanced")
        messages = build_generate_messages(state)
        assert "advanced" in messages[0].content

    def test_no_forbidden_words_in_prompt(self):
        from langchain_core.documents import Document
        state = self._make_state(retrieved_docs=[
            Document(page_content="test", metadata={})
        ])
        messages = build_generate_messages(state)
        forbidden = ["向量数据库", "RAG", "参考内容", "根据资料", "知识库", "检索"]
        for word in forbidden:
            assert word not in messages[0].content, f"Forbidden word found: {word}"


class TestGenerateNode:
    def _make_state(self, **overrides):
        state = {
            "query": "test",
            "chat_history": [],
            "retrieved_docs": [],
            "grade_result": "relevant",
            "rewritten_query": "",
            "rewrite_count": 0,
            "answer": "",
            "user_level": "intermediate",
        }
        state.update(overrides)
        return state

    @pytest.mark.asyncio
    async def test_generate_node_success(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.return_value = MagicMock(content="This is the answer.")
        monkeypatch.setattr("graph.nodes.generate._llm", mock_llm)
        from graph.nodes.generate import generate_node
        result = await generate_node(self._make_state())
        assert result["answer"] == "This is the answer."

    @pytest.mark.asyncio
    async def test_generate_node_llm_uninitialized(self, monkeypatch):
        monkeypatch.setattr("graph.nodes.generate._llm", None)
        from graph.nodes.generate import generate_node
        result = await generate_node(self._make_state())
        assert "未初始化" in result["answer"]

    @pytest.mark.asyncio
    async def test_generate_node_exception(self, monkeypatch):
        mock_llm = MagicMock()
        mock_llm.invoke.side_effect = Exception("Rate limit exceeded")
        monkeypatch.setattr("graph.nodes.generate._llm", mock_llm)
        from graph.nodes.generate import generate_node
        result = await generate_node(self._make_state())
        assert "暂时繁忙" in result["answer"]
