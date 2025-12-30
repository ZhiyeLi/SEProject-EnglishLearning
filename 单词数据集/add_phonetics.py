import argparse
import csv
import os
import time
from typing import Optional

import requests

SYSTEM_PROMPT = (
    "You return only the IPA phonetic transcription for a given English word. "
    "Prefer standard British English unless a different accent is specified. "
    "Output only the phonetic transcription with no extra text."
)


def fetch_phonetic(word: str, pos: str, api_key: str, model: str, endpoint: str, retries: int = 3) -> str:
    """Call the DeepSeek chat API to fetch the IPA phonetic transcription."""
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {
                "role": "user",
                # Only send the word to reduce latency and token usage.
                "content": f"Word: {word}\nReturn IPA only.",
            },
        ],
        "temperature": 0.0,
    }

    last_error: Optional[str] = None
    for attempt in range(1, retries + 1):
        try:
            resp = requests.post(endpoint, headers=headers, json=payload, timeout=30)
            if resp.status_code == 429:
                # Back off on rate limits.
                time.sleep(1.5 * attempt)
                continue
            resp.raise_for_status()
            data = resp.json()
            content = data["choices"][0]["message"]["content"].strip()
            return content
        except Exception as exc:  # noqa: BLE001
            last_error = str(exc)
            time.sleep(1.2 * attempt)

    raise RuntimeError(f"Failed to fetch phonetic for '{word}': {last_error}")


def enrich_file(
    input_path: str,
    output_path: str,
    api_key: str,
    model: str,
    endpoint: str,
    limit: Optional[int],
    sleep_seconds: float,
) -> None:
    processed_missing = 0

    with open(input_path, newline="", encoding="utf-8") as infile:
        reader = csv.DictReader(infile)
        fieldnames = reader.fieldnames or []
        if "phonetic" not in fieldnames:
            raise ValueError("Input file must contain a 'phonetic' column")

        rows = list(reader)
        total_rows = len(rows)

        for idx, row in enumerate(rows):
            phonetic = (row.get("phonetic") or "").strip()
            if not phonetic and (limit is None or processed_missing < limit):
                word = (row.get("word") or "").strip()
                if not word:
                    continue

                try:
                    new_phonetic = fetch_phonetic(word, "", api_key, model, endpoint)
                    row["phonetic"] = new_phonetic
                    processed_missing += 1
                except Exception as exc:  # noqa: BLE001
                    row["phonetic"] = phonetic
                    print(f"Skipping '{word}' due to error: {exc}")
                time.sleep(sleep_seconds)

            if (idx + 1) % 50 == 0 or idx + 1 == total_rows:
                print(f"Progress: {idx + 1}/{total_rows} rows processed; filled {processed_missing}", flush=True)

    with open(output_path, "w", newline="", encoding="utf-8") as outfile:
        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    print(
        f"Completed. Total rows: {total_rows}. "
        f"Filled: {processed_missing}. Output: {output_path}"
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Fill missing phonetic data using DeepSeek chat API.")
    parser.add_argument("--input", default="oxford3000_filtered.csv", help="Input CSV file with a 'phonetic' column.")
    parser.add_argument(
        "--output",
        default="oxford3000_with_phonetic.csv",
        help="Path to write enriched CSV. Defaults to input name with suffix.",
    )
    parser.add_argument("--model", default="deepseek-chat", help="DeepSeek model name.")
    parser.add_argument(
        "--endpoint",
        default="https://api.deepseek.com/chat/completions",
        help="DeepSeek chat completions endpoint.",
    )
    parser.add_argument("--limit", type=int, default=None, help="Max number of missing phonetics to fill (for testing).")
    parser.add_argument(
        "--sleep",
        type=float,
        default=0.6,
        help="Seconds to sleep between API calls to reduce rate limits.",
    )
    parser.add_argument(
        "--api-key",
        dest="api_key",
        default=os.getenv("DEEPSEEK_API_KEY"),
        help="API key. Defaults to DEEPSEEK_API_KEY env variable.",
    )
    args = parser.parse_args()

    if not args.api_key:
        raise SystemExit("API key is required. Set --api-key or DEEPSEEK_API_KEY.")

    enrich_file(
        input_path=args.input,
        output_path=args.output,
        api_key=args.api_key,
        model=args.model,
        endpoint=args.endpoint,
        limit=args.limit,
        sleep_seconds=args.sleep,
    )


if __name__ == "__main__":
    main()
