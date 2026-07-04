# rag_service/memory/checkpoint.py
import os
import logging
from langgraph.checkpoint.sqlite import SqliteSaver

logger = logging.getLogger(__name__)


def get_checkpointer(db_path: str = None) -> SqliteSaver:
    if db_path is None:
        db_path = os.path.join(os.path.dirname(__file__), '..', 'checkpoints.db')
    logger.info(f"Initializing SqliteSaver at {db_path}")
    return SqliteSaver.from_conn_string(db_path)
