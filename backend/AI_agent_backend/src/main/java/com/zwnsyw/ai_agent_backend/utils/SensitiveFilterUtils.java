package com.zwnsyw.ai_agent_backend.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载敏感词：将敏感词从 resources 目录下的文件加载到内存中。
 * 集成过滤器：在评论提交时，使用过滤器对评论内容进行检查，替换敏感词为 ***。
 * 返回过滤后的评论：将过滤后的评论返回给前端。
 */
@Component
public class SensitiveFilterUtils {
    private static final String REPLACEMENT = "***";
    private static final String SENSITIVE_WORDS_FILE = "sensitive-words.txt";
    private final TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        // 添加空值检查
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SENSITIVE_WORDS_FILE);

        // 如果文件不存在，则记录日志并返回，避免空指针异常
        if (inputStream == null) {
            System.err.println("警告: 未找到敏感词文件 '" + SENSITIVE_WORDS_FILE + "'，敏感词过滤功能将不可用");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String word;
            while ((word = reader.readLine()) != null) {
                // 过滤空行和注释行（可选功能）
                word = word.trim();
                if (!word.isEmpty() && !word.startsWith("#")) {
                    addSensitiveWord(word);
                }
            }
        } catch (IOException e) {
            System.err.println("读取敏感词文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addSensitiveWord(String word) {
        TrieNode currentNode = rootNode;
        for (char c : word.toCharArray()) {
            currentNode = currentNode.getChildren().computeIfAbsent(c, k -> new TrieNode());
        }
        currentNode.setEndOfWord(true);
    }

    public String filter(String text) {
        // 处理空文本情况
        if (text == null || text.isEmpty()) {
            return text;
        }

        TrieNode currentNode = rootNode;
        StringBuilder result = new StringBuilder();
        int begin = 0;
        int end = 0;

        while (end < text.length()) {
            char c = text.charAt(end);
            if (isSymbol(c)) {
                if (currentNode == rootNode) {
                    result.append(c);
                    begin++;
                }
                end++;
                continue;
            }

            currentNode = currentNode.getChildren().get(c);
            if (currentNode == null) {
                result.append(text.charAt(begin));
                end = ++begin;
                currentNode = rootNode;
            } else if (currentNode.isEndOfWord()) {
                result.append(REPLACEMENT);
                begin = ++end;
                currentNode = rootNode;
            } else {
                end++;
            }
        }
        result.append(text.substring(begin));
        return result.toString();
    }

    private boolean isSymbol(char c) {
        return !Character.isLetterOrDigit(c);
    }

    private static class TrieNode {
        private boolean endOfWord;
        private final Map<Character, TrieNode> children = new HashMap<>();

        public boolean isEndOfWord() {
            return endOfWord;
        }

        public void setEndOfWord(boolean endOfWord) {
            this.endOfWord = endOfWord;
        }

        public Map<Character, TrieNode> getChildren() {
            return children;
        }
    }
}
