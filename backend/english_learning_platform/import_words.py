#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import csv
import sys

# 映射文件和type_id的对应关系
files_mapping = {
    r'C:\Users\31662\Desktop\SEProject-EnglishLearning\单词数据集\oxford3000_with_phonetic.csv': 1,  # 基础单词
    r'C:\Users\31662\Desktop\SEProject-EnglishLearning\单词数据集\cet4_with_phonetic.csv': 2,  # 四级单词
    r'C:\Users\31662\Desktop\SEProject-EnglishLearning\单词数据集\cet6_with_phonetic.csv': 3,  # 六级单词
    r'C:\Users\31662\Desktop\SEProject-EnglishLearning\单词数据集\toefl_ielts_with_phonetic.csv': 4,  # 托福雅思
}

output_file = r'C:\Users\31662\Desktop\SEProject-EnglishLearning\backend\english_learning_platform\import_words.sql'

def truncate_field(text, max_len):
    """截断字段到指定长度"""
    if not text:
        return ''
    text = text.strip()
    return text[:max_len] if len(text) > max_len else text

# 生成SQL语句
with open(output_file, 'w', encoding='utf-8') as out:
    out.write('-- 导入单词数据\n')
    out.write('SET FOREIGN_KEY_CHECKS=0;\n')
    out.write('DELETE FROM words;\n')
    out.write('SET FOREIGN_KEY_CHECKS=1;\n\n')
    
    word_id = 1
    
    for csv_file, type_id in files_mapping.items():
        print(f"处理文件: {csv_file} (type_id: {type_id})")
        
        try:
            with open(csv_file, 'r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                
                for row in reader:
                    word = truncate_field(row.get('word', ''), 255)
                    part_of_speech = truncate_field(row.get('part_of_speech', ''), 255)
                    phonetic = truncate_field(row.get('phonetic', ''), 255)
                    definition = truncate_field(row.get('definition', ''), 500)
                    example = truncate_field(row.get('example', ''), 500)
                    synonyms = truncate_field(row.get('synonyms', ''), 500)
                    antonyms = truncate_field(row.get('antonyms', ''), 500)
                    usage_notes = truncate_field(row.get('usage_notes', ''), 500)
                    
                    # 转义单引号和反斜杠
                    word = word.replace('\\', '\\\\').replace("'", "\\'")
                    part_of_speech = part_of_speech.replace('\\', '\\\\').replace("'", "\\'")
                    phonetic = phonetic.replace('\\', '\\\\').replace("'", "\\'")
                    definition = definition.replace('\\', '\\\\').replace("'", "\\'")
                    example = example.replace('\\', '\\\\').replace("'", "\\'")
                    synonyms = synonyms.replace('\\', '\\\\').replace("'", "\\'")
                    antonyms = antonyms.replace('\\', '\\\\').replace("'", "\\'")
                    usage_notes = usage_notes.replace('\\', '\\\\').replace("'", "\\'")
                    
                    sql = f"INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES ({word_id}, '{word}', '{part_of_speech}', '{phonetic}', '{definition}', '{example}', {type_id}, '{synonyms}', '{antonyms}', '{usage_notes}', NULL, NULL);\n"
                    
                    out.write(sql)
                    word_id += 1
        
        except Exception as e:
            print(f"错误: {e}")
            sys.exit(1)
    
    print(f"\n✅ 已生成 {output_file}")
    print(f"共处理 {word_id - 1} 个单词")

print("完成！")
