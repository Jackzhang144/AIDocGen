import React, { useEffect, useRef, useState } from 'react';
import Editor from '@monaco-editor/react';
import { Box, Paper, Button, Stack, CircularProgress, Typography, Divider } from '@mui/material';
import { Comment, Description, AutoStories, Save, Replay, Science, Code } from '@mui/icons-material';
import axios from 'axios';
import { API_BASE_URL } from '../config';

export default function CodeEditor({ activeFile, setActiveFile, onAiResult, lang, strings, token, role, usage, onUsageRefresh }) {
    const editorRef = useRef(null);
    const [loading, setLoading] = useState(false);
    const [pendingRewrite, setPendingRewrite] = useState(null);

    const handleEditorDidMount = (editor) => {
        editorRef.current = editor;
    };

    useEffect(() => {
        // 根据 jumpToLine 信息跳转
        if (!editorRef.current || !activeFile?.jumpToLine) return;
        const line = activeFile.jumpToLine;
        editorRef.current.revealLineInCenter(line);
        editorRef.current.setSelection({
            startLineNumber: line,
            startColumn: 1,
            endLineNumber: line,
            endColumn: 1,
        });
        // Clear jumpToLine after jumping to prevent re-triggering
        setActiveFile(prev => ({ ...prev, jumpToLine: null }));
    }, [activeFile, setActiveFile]);

    useEffect(() => {
        setPendingRewrite(null);
    }, [activeFile?.path]);

    const getSelectedText = () => {
        const editor = editorRef.current;
        if (!editor) return '';
        const selection = editor.getSelection();
        if (!selection || selection.isEmpty()) return '';
        return editor.getModel().getValueInRange(selection);
    };

    const replaceSelection = (text, rangeOverride) => {
        const editor = editorRef.current;
        const selection = rangeOverride || editor?.getSelection();
        if (!editor || !selection) return;
        editor.executeEdits('ai-comment', [{ range: selection, text }]);
        // Adjust selection to span the newly inserted/rewritten text
        const endLineNumber = selection.startLineNumber + text.split('\n').length - 1;
        const endColumn = text.split('\n').slice(-1)[0].length + 1;

        editor.setSelection({
            startLineNumber: selection.startLineNumber,
            startColumn: selection.startColumn,
            endLineNumber: endLineNumber,
            endColumn: endColumn,
        });
        editor.focus();
    };

    // 通用 AI 调用函数
    const callAi = async (type) => {
        if (!activeFile) return;
        setLoading(true);
        let code = editorRef.current.getValue();
        const selection = editorRef.current.getSelection()?.toJSON();
        let snippet = code;

        // Code and snippet extraction logic
        if (type === 'explain' || type === 'comment' || type === 'rewrite') {
            const selected = getSelectedText();
            if (!selected || !selected.trim()) {
                alert(strings[`selectFor${type.charAt(0).toUpperCase() + type.slice(1)}`]);
                setLoading(false);
                return;
            }
            code = selected;
            snippet = selected;
        }

        if (type === 'explain') {
            onAiResult({ content: strings.generatingExplain, title: strings.aiExplain, type, snippet: snippet, fileName: activeFile.name });
        }

        try {
            const res = await axios.post(`${API_BASE_URL}/api/ai/process`, {
                type,
                code,
                fileName: activeFile.name,
                // For 'document', we send the entire file content as context
                context: type === 'document' ? editorRef.current.getValue() : (activeFile.content || ""),
                language: lang,
                filePath: activeFile.path,
                framework: inferFramework(activeFile.path),
                editorLanguage: detectEditorLanguage(activeFile?.name),
            }, {
                headers: {
                    Authorization: token ? `Bearer ${token}` : undefined,
                },
            });

            const result = res.data;

            if (type === 'comment') {
                // 注释模式：替换选中区域
                replaceSelection(result);
                // 更新本地状态
                const updatedContent = editorRef.current.getValue();
                setActiveFile({ ...activeFile, content: updatedContent });
                onAiResult({ content: result, title: strings.generateComment, type, snippet: snippet, fileName: activeFile.name, snippetType: 'commentPreview' });
            } else if (type === 'explain') {
                onAiResult({ content: result, title: strings.aiExplain, type, snippet: code, fileName: activeFile.name, snippetType: 'explainPreview' });
            } else if (type === 'document') {
                onAiResult({ content: result, title: strings.aiDoc, type, snippet: snippet, fileName: activeFile.name, snippetType: 'snippetPreview' });
            } else if (type === 'rewrite') {
                const diff = buildDiff(code, result);
                setPendingRewrite({ replacement: result, original: code, selection, diff });
                // Pass the diff result to AiPanel for preview
                onAiResult({ content: `\`\`\`diff\n${diff}\n\`\`\``, title: strings.rewritePreview, type, snippet: code, fileName: activeFile.name, snippetType: 'rewritePreview' });
            } else if (type === 'test') {
                onAiResult({ content: result, title: strings.testDraft, type, snippet: code, fileName: activeFile.name, snippetType: 'snippetPreview' });
            }
        } catch (err) {
            console.error(err);
            const msg = err.response?.data?.message || err.message || strings.aiRequestFailed || "AI 请求失败，请检查后端服务是否启动。";
            const failTitle = type === 'document' ? strings.aiDoc : (type === 'test' ? strings.testDraft : strings.aiExplain);
            onAiResult({ content: `**Error:** ${msg}`, title: failTitle, type, snippet: code, snippetType: 'snippetPreview' });
            alert(msg);
        } finally {
            setLoading(false);
            onUsageRefresh?.();
        }
    };

    const saveFile = async () => {
        if (!activeFile) return;
        const val = editorRef.current.getValue();
        try {
            const writable = await activeFile.handle.createWritable();
            await writable.write(val);
            await writable.close();
            // Update activeFile content to reflect saved state
            setActiveFile(prev => ({ ...prev, content: val }));
            alert("保存成功");
        } catch(e) {
            alert("保存失败: " + e.message);
        }
    };

    const applyRewrite = () => {
        if (!pendingRewrite || !pendingRewrite.selection) return;
        replaceSelection(pendingRewrite.replacement, pendingRewrite.selection);
        const updatedContent = editorRef.current.getValue();
        setActiveFile({ ...activeFile, content: updatedContent });
        setPendingRewrite(null);
    };

    // 移除了 runSnippet 函数
    // 移除了 runJavascript 函数

    const buildDiff = (before, after) => {
        const beforeLines = (before || '').split('\n');
        const afterLines = (after || '').split('\n');
        const max = Math.max(beforeLines.length, afterLines.length);
        const diffLines = [];
        for (let i = 0; i < max; i += 1) {
            const left = beforeLines[i] ?? '';
            const right = afterLines[i] ?? '';
            if (left === right) {
                diffLines.push('  ' + left);
            } else {
                // If both lines exist but are different, show both changes
                if (left) diffLines.push('- ' + left);
                if (right) diffLines.push('+ ' + right);
            }
        }
        return diffLines.join('\n');
    };

    // 修复: 扩展 detectEditorLanguage 函数，支持更多语言
    const detectEditorLanguage = (fileName = '') => {
        const lowerCaseFileName = fileName.toLowerCase();

        if (lowerCaseFileName.endsWith('.java')) return 'java';
        if (lowerCaseFileName.endsWith('.ts') || lowerCaseFileName.endsWith('.tsx')) return 'typescript';
        if (lowerCaseFileName.endsWith('.js') || lowerCaseFileName.endsWith('.jsx')) return 'javascript';
        if (lowerCaseFileName.endsWith('.json')) return 'json';
        if (lowerCaseFileName.endsWith('.css')) return 'css';
        if (lowerCaseFileName.endsWith('.html') || lowerCaseFileName.endsWith('.htm')) return 'html';
        if (lowerCaseFileName.endsWith('.md')) return 'markdown';
        if (lowerCaseFileName.endsWith('.yaml') || lowerCaseFileName.endsWith('.yml')) return 'yaml';
        if (lowerCaseFileName.endsWith('.sh') || lowerCaseFileName.endsWith('.bash')) return 'shell';
        if (lowerCaseFileName.endsWith('.py')) return 'python';
        if (lowerCaseFileName.endsWith('.go')) return 'go';
        if (lowerCaseFileName.endsWith('.xml')) return 'xml';
        if (lowerCaseFileName.endsWith('.c') || lowerCaseFileName.endsWith('.h')) return 'c';
        if (lowerCaseFileName.endsWith('.cpp') || lowerCaseFileName.endsWith('.hpp')) return 'cpp';

        return 'plaintext'; // Default fallback
    };

    const inferFramework = (filePath = '') => {
        if (filePath.includes('backend')) return 'Spring Boot';
        if (filePath.includes('frontend')) return 'React + Vite';
        return '';
    };

    if (!activeFile) {
        return (
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', bgcolor: 'background.default' }}>
                <Paper elevation={3} sx={{ p: 4, borderRadius: 2, textAlign: 'center' }}>
                    <Code sx={{ fontSize: 40, color: 'text.secondary' }} />
                    <Typography variant="h6" color="text.secondary" mt={1}>
                        {strings.selectFile}
                    </Typography>
                </Paper>
            </Box>
        );
    }

    return (
        <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'background.default' }}>
            {/* 工具栏 - Use Paper for a floating/clean look */}
            <Paper square elevation={1} sx={{ p: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #E0E0E0' }}>
                <Box>
                    <Typography variant="subtitle1" fontWeight="bold">{activeFile.name}</Typography>
                </Box>
                <Stack direction="row" spacing={1}>
                    <Button startIcon={<Save />} size="small" variant="outlined" onClick={saveFile} disabled={loading}>{strings.save}</Button>
                    <Button variant="contained" startIcon={<Comment />} size="small" onClick={() => callAi('comment')} disabled={loading || !token}>
                        {strings.generateComment}
                    </Button>
                    <Button variant="outlined" startIcon={<Description />} size="small" onClick={() => callAi('explain')} disabled={loading || !token}>
                        {strings.explainCode}
                    </Button>
                    <Button variant="outlined" startIcon={<AutoStories />} size="small" onClick={() => callAi('document')} disabled={loading || !token}>
                        {strings.generateDoc}
                    </Button>
                    <Button variant="outlined" startIcon={<Science />} size="small" onClick={() => callAi('test')} disabled={loading || !token}>
                        {strings.generateTest}
                    </Button>
                    <Button variant="outlined" startIcon={<Replay />} size="small" onClick={() => callAi('rewrite')} disabled={loading || !token}>
                        {strings.rewriteCode}
                    </Button>
                    {pendingRewrite ? (
                        <Button color="success" size="small" variant="contained" onClick={applyRewrite}>{strings.applyRewrite}</Button>
                    ) : null}
                    {loading && <CircularProgress size={24} sx={{ ml: 1 }} />}
                </Stack>
            </Paper>

            {/* Monaco Editor (占满剩余空间) */}
            <Box sx={{ flexGrow: 1, display: 'flex', border: '1px solid #E0E0E0', borderTop: 'none' }}>
                <Editor
                    height="100%"
                    defaultLanguage={detectEditorLanguage(activeFile.name)}
                    value={activeFile.content}
                    theme="vs-light"
                    onMount={handleEditorDidMount}
                    options={{
                        minimap: { enabled: true },
                        fontSize: 14,
                        scrollBeyondLastLine: false,
                    }}
                />
            </Box>
        </Box>
    );
}
