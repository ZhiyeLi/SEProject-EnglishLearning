import os
import glob
import faiss
import pickle
import numpy as np
import pandas as pd
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from retrieval.embeddings import EmbeddingService
from dotenv import load_dotenv

load_dotenv()

LEVEL_LABELS = {
    "四级词汇": "CET-4",
    "六级词汇": "CET-6",
    "牛津3000": "Oxford 3000",
    "托福雅思": "TOEFL/IELTS",
}

# ========== 单词文档格式化 ==========

def format_word_document(row, source_file):
    word = str(row.get("word", "")).strip()
    pos = str(row.get("part_of_speech", "")).strip()
    phonetic = str(row.get("phonetic", "")).strip()
    definition = str(row.get("definition", "")).strip()
    example = str(row.get("example", "")).strip()
    level = str(row.get("level", "")).strip()
    synonyms = str(row.get("synonyms", "")).strip()
    antonyms = str(row.get("antonyms", "")).strip()

    if not word or not definition:
        return None

    parts = [f'单词: {word}']
    if pos:
        parts.append(f'词性: {pos}')
    if phonetic:
        parts.append(f'音标: {phonetic}')
    parts.append(f'释义: {definition}')
    if example and example.lower() != "nan":
        parts.append(f'例句: {example}')
    if synonyms and synonyms.lower() != "nan":
        parts.append(f'近义词: {synonyms}')
    if antonyms and antonyms.lower() != "nan":
        parts.append(f'反义词: {antonyms}')
    if level:
        label = LEVEL_LABELS.get(level, level)
        parts.append(f'等级: {label}')

    text = "\n".join(parts)
    return Document(
        page_content=text,
        metadata={
            "source_type": "单词",
            "word": word,
            "level": level,
            "source": os.path.basename(source_file),
        },
    )

# ========== 语法文档格式化 ==========

def format_grammar_document(row, source_file):
    topic = str(row.get("topic", "")).strip()
    category = str(row.get("category", "")).strip()
    level = str(row.get("level", "")).strip()
    explanation = str(row.get("explanation", "")).strip()
    example = str(row.get("example", "")).strip()
    key_points = str(row.get("key_points", "")).strip()

    if not topic or not explanation:
        return None

    parts = [f'语法点: {topic}']
    if category:
        parts.append(f'类别: {category}')
    if level:
        parts.append(f'适用等级: {level}')
    parts.append(f'讲解: {explanation}')
    if example and example.lower() != "nan":
        parts.append(f'例句: {example}')
    if key_points and key_points.lower() != "nan":
        parts.append(f'重点提示: {key_points}')

    text = "\n".join(parts)
    return Document(
        page_content=text,
        metadata={
            "source_type": "语法",
            "topic": topic,
            "category": category,
            "level": level,
            "source": os.path.basename(source_file),
        },
    )

# ========== 作文模板格式化 ==========

def format_writing_document(row, source_file):
    template_type = str(row.get("template_type", "")).strip()
    exam_level = str(row.get("exam_level", "")).strip()
    title = str(row.get("title", "")).strip()
    structure = str(row.get("structure", "")).strip()
    useful_expressions = str(row.get("useful_expressions", "")).strip()
    sample_sentence = str(row.get("sample_sentence", "")).strip()
    notes = str(row.get("notes", "")).strip()

    if not title:
        return None

    parts = [f'作文模板: {title}']
    if template_type:
        parts.append(f'文体类型: {template_type}')
    if exam_level:
        parts.append(f'适用考试: {exam_level}')
    if structure and structure.lower() != "nan":
        parts.append(f'段落结构:\n{structure}')
    if useful_expressions and useful_expressions.lower() != "nan":
        parts.append(f'常用表达:\n{useful_expressions}')
    if sample_sentence and sample_sentence.lower() != "nan":
        parts.append(f'范文示例: {sample_sentence}')
    if notes and notes.lower() != "nan":
        parts.append(f'注意事项: {notes}')

    text = "\n".join(parts)
    return Document(
        page_content=text,
        metadata={
            "source_type": "作文模板",
            "template_type": template_type,
            "exam_level": exam_level,
            "title": title,
            "source": os.path.basename(source_file),
        },
    )

# ========== 主构建函数 ==========

def load_csv_documents(data_dir, format_fn, description):
    """从指定目录加载所有CSV文件并格式化为Document列表"""
    csv_files = glob.glob(os.path.join(data_dir, '*.csv'))
    documents = []
    print(f"加载{description}...")
    for file in csv_files:
        print(f"  Loading {os.path.basename(file)}...")
        try:
            df = pd.read_csv(file, encoding='utf-8-sig')
            for _, row in df.iterrows():
                doc = format_fn(row, file)
                if doc:
                    documents.append(doc)
        except Exception as e:
            print(f"  Error loading {file}: {e}")
    print(f"  {description}共加载 {len(documents)} 条")
    return documents


def build_index():
    all_documents = []

    # 数据目录配置: (目录名, 格式化函数, 描述)
    project_root = os.path.join(os.path.dirname(__file__), '..')
    data_sources = [
        ('单词数据集', format_word_document, '单词'),
        ('语法数据集', format_grammar_document, '语法'),
        ('作文模板数据集', format_writing_document, '作文模板'),
    ]

    for dir_name, format_fn, desc in data_sources:
        data_dir = os.path.join(project_root, dir_name)
        if os.path.exists(data_dir):
            docs = load_csv_documents(data_dir, format_fn, desc)
            all_documents.extend(docs)
        else:
            print(f"警告: 目录 {data_dir} 不存在，跳过{desc}数据")

    if not all_documents:
        print("未加载到任何文档，请检查数据文件。")
        return

    print(f"\n共加载 {len(all_documents)} 条文档。")

    # 根据内容类型用不同策略切割
    # 单词和语法条目较短，作文模板较长
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=800, chunk_overlap=80)
    texts = text_splitter.split_documents(all_documents)
    print(f"切割后共 {len(texts)} 个文本块。")

    # BGE-M3 embedding
    print("正在初始化 BGE-M3 Embedding 模型（首次运行会自动下载 ~2.2GB）...")
    emb_service = EmbeddingService()

    page_contents = [doc.page_content for doc in texts]

    # Fit TF-IDF on full corpus first (for sparse lexical weights)
    emb_service.fit_tfidf(page_contents)

    print(f"正在编码 {len(page_contents)} 个文本块...")
    vecs = emb_service.embed_documents(page_contents)

    dense_vectors = vecs["dense"]
    sparse_vectors = vecs["sparse"]

    # GPU tensor → CPU numpy (FAISS requires numpy arrays)
    import torch
    if hasattr(dense_vectors, "cpu"):
        dense_vectors = dense_vectors.cpu().numpy().astype("float32")

    # Build FAISS dense index
    print(f"正在构建 FAISS 稠密索引 (dim={dense_vectors.shape[1]})...")
    faiss.normalize_L2(dense_vectors)
    dense_index = faiss.IndexFlatIP(dense_vectors.shape[1])
    dense_index.add(dense_vectors)

    # Save
    index_dir = os.path.join(os.path.dirname(__file__), 'faiss_index')
    os.makedirs(index_dir, exist_ok=True)

    dense_path = os.path.join(index_dir, 'faiss_index')
    faiss.write_index(dense_index, dense_path)
    print(f"FAISS 稠密索引已保存至: {dense_path}")

    sparse_path = os.path.join(index_dir, 'sparse_vectors.pkl')
    with open(sparse_path, 'wb') as f:
        pickle.dump(sparse_vectors, f)
    print(f"稀疏向量已保存至: {sparse_path}")

    docs_path = os.path.join(index_dir, 'documents.pkl')
    with open(docs_path, 'wb') as f:
        pickle.dump(texts, f)
    print(f"文档已保存至: {docs_path}")

    print(f"\n索引构建完成！")
    print(f"  - 文档数: {len(texts)}")
    print(f"  - 稠密维度: {dense_vectors.shape[1]}")
    print(f"  - 稀疏词表大小: 每个文档独立词权重")


if __name__ == "__main__":
    build_index()
