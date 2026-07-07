# rag_service/memory/checkpoint.py
import os
import logging
from langgraph.checkpoint.sqlite import SqliteSaver

logger = logging.getLogger(__name__)


def get_checkpointer(db_path: str = None):
    """Create and return a SqliteSaver instance.

    Returns a tuple of (SqliteSaver, context_manager) — the context manager
    must be kept alive for the saver to work.
    """
    import sqlite3
    if db_path is None:
        db_path = os.path.join(os.path.dirname(__file__), '..', 'checkpoints.db')
    logger.info(f"Initializing SqliteSaver at {db_path}")
    conn = sqlite3.connect(db_path, check_same_thread=False)
    saver = SqliteSaver(conn)
    # Set up WAL mode for concurrent access
    conn.execute("PRAGMA journal_mode=WAL")
    return saver
