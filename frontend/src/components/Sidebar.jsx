import React, { useState } from 'react';
import { List, ListItemButton, ListItemText, ListItemIcon, Button, Box, Divider, TextField, Typography, Stack } from '@mui/material';
import { FolderOpen, InsertDriveFile, Folder, ExpandLess, ExpandMore, Search } from '@mui/icons-material';

export default function Sidebar({ width, onFileSelect, setDirectoryHandle, strings }) {
    const [files, setFiles] = useState([]); // 扁平化的列表，包含 depth/expanded 信息
    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [searching, setSearching] = useState(false);

    // 打开文件夹逻辑
    const handleOpenDir = async () => {
        try {
            const handle = await window.showDirectoryPicker();
            setDirectoryHandle(handle);
            const fileList = await readDirectory(handle);
            setFiles(fileList);
        } catch (err) {
            console.error(err);
        }
    };

    // 读取目录的直接子项（不递归），返回带 depth 信息的节点列表
    const readDirectory = async (dirHandle, path = '', depth = 0) => {
        const entries = [];
        for await (const entry of dirHandle.values()) {
            const base = { name: entry.name, path: path + entry.name, depth };
            if (entry.kind === 'file') {
                entries.push({ ...base, kind: 'file', handle: entry });
            } else if (entry.kind === 'directory') {
                entries.push({
                    ...base,
                    kind: 'dir',
                    handle: entry,
                    expanded: false,
                    childrenLoaded: false,
                });
            }
        }
        // Exclude system/hidden files like .git, node_modules, dist, etc. for a cleaner view
        const filteredEntries = entries.filter(e => !e.name.startsWith('.') && e.name !== 'node_modules' && e.name !== 'dist');
        return filteredEntries.sort((a, b) => (a.kind === 'dir' && b.kind !== 'dir' ? -1 : 1));
    };

    const handleFileClick = async (file) => {
        if (file.kind === 'dir') {
            await toggleDirectory(file);
            return;
        }
        const fileData = await file.handle.getFile();
        const text = await fileData.text();
        // 如果是从搜索结果点击的文件，会携带 jumpToLine 信息
        onFileSelect({ ...file, content: text, jumpToLine: file.jumpToLine });
    };

    // 展开/折叠目录，使用扁平列表插入/移除子节点
    const toggleDirectory = async (dirNode) => {
        const idx = files.findIndex((n) => n.path === dirNode.path && n.depth === dirNode.depth);
        if (idx === -1) return;

        // 已展开则折叠并移除子孙
        if (files[idx].expanded) {
            setFiles((prev) => {
                const prefix = dirNode.path + '/';
                const next = prev.filter((node, i) => i <= idx || !node.path.startsWith(prefix));
                next[idx] = { ...next[idx], expanded: false };
                return next;
            });
            return;
        }

        // 未展开：读取或使用缓存子节点，再插入
        let children = files[idx].children && files[idx].childrenLoaded
            ? files[idx].children
            : await readDirectory(dirNode.handle, dirNode.path + '/', dirNode.depth + 1);

        setFiles((prev) => {
            const next = [...prev];
            const current = next[idx] || dirNode;
            // Update the children in the node itself for future re-expansion without re-reading
            next[idx] = { ...current, expanded: true, childrenLoaded: true, children };
            next.splice(idx + 1, 0, ...children);
            return next;
        });
    };

    const renderChevron = (file) => {
        if (file.kind !== 'dir') return <Box sx={{ width: 16 }} />;
        // Use default icons, but change color to primary for better visibility in light theme
        return file.expanded ? <ExpandLess color="primary" /> : <ExpandMore color="primary" />;
    };

    // 升级搜索功能
    const handleSearch = async () => {
        if (!searchTerm.trim()) {
            setSearchResults([]);
            return;
        }
        setSearching(true);
        const results = [];
        const keyword = searchTerm.toLowerCase();

        // 使用 Set 来避免重复的结果（同一文件同一行）
        const seenMatches = new Set();

        for (const file of files.filter((f) => f.kind === 'file')) {
            const fileNameLower = file.name.toLowerCase();
            const filenameMatch = fileNameLower.includes(keyword);

            // 1. 检查文件名匹配
            if (filenameMatch) {
                const key = file.path + ':name';
                if (!seenMatches.has(key)) {
                    results.push({
                        ...file,
                        jumpToLine: 0, // 0 表示文件名匹配
                        snippet: strings.matchFileName || '文件名匹配',
                        matchType: 'name',
                    });
                    seenMatches.add(key);
                }
            }

            // 2. 检查文件内容匹配
            try {
                const data = await file.handle.getFile();
                const text = await data.text();
                const lines = text.split('\n');

                lines.forEach((line, index) => {
                    const lineLower = line.toLowerCase();
                    if (lineLower.includes(keyword)) {
                        const lineNumber = index + 1;
                        const key = file.path + ':' + lineNumber;

                        if (!seenMatches.has(key)) {
                            const startIndex = lineLower.indexOf(keyword);
                            const endIndex = startIndex + keyword.length;

                            // 提取并高亮关键词前后120个字符的片段
                            let snippetText = line.trim();
                            if (snippetText.length > 120) {
                                // 截取片段，使其突出显示匹配项
                                const highlight = line.substring(startIndex, endIndex);
                                const contextStart = Math.max(0, startIndex - 60);
                                const contextEnd = Math.min(line.length, endIndex + 60);

                                snippetText = line.substring(contextStart, contextEnd).trim();
                                if (contextStart > 0) snippetText = '...' + snippetText;
                                if (contextEnd < line.length) snippetText = snippetText + '...';
                            }

                            results.push({
                                ...file,
                                jumpToLine: lineNumber,
                                snippet: snippetText,
                                matchType: 'content',
                            });
                            seenMatches.add(key);
                        }
                    }
                });

            } catch (e) {
                // 忽略读取大文件或二进制文件失败
                console.warn(`Error reading file content for search in ${file.name}:`, e);
            }
        }

        setSearchResults(results);
        setSearching(false);
    };

    return (
        // 修复: 替换 Drawer 为固定定位的 Box (Sidebar)
        <Box
            sx={{
                width: width,
                flexShrink: 0,
                position: 'fixed', // 使用固定定位以不影响主内容流
                left: 0,
                top: 0,
                height: '100vh',
                boxSizing: 'border-box',
                pt: '56px',
                zIndex: (theme) => theme.zIndex.drawer, // 确保在主内容之上但在 AppBar 之下
                backgroundColor: 'background.paper', // 匹配主题中的 Drawer/Paper 背景
                borderRight: '1px solid #E0E0E0',
            }}
        >
            <Box sx={{ p: 2, bgcolor: 'background.paper', borderBottom: '1px solid #E0E0E0' }}> {/* Use Paper/Box for visual grouping */}
                <Button variant="contained" startIcon={<FolderOpen />} fullWidth onClick={handleOpenDir} sx={{ mb: 2 }}>
                    {strings?.openProject || '打开项目'}
                </Button>
                <Stack spacing={1}>
                    <TextField
                        size="small"
                        placeholder={strings?.searchPlaceholder || '搜索'}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onKeyDown={(e) => { if (e.key === 'Enter') handleSearch(); }}
                        InputProps={{
                            startAdornment: (
                                <ListItemIcon sx={{ minWidth: 0, mr: 1, color: 'text.secondary' }}>
                                    <Search fontSize="small" />
                                </ListItemIcon>
                            ),
                        }}
                    />
                    <Button size="small" fullWidth onClick={handleSearch} disabled={searching} variant="outlined">
                        {searching ? (strings?.searching || '搜索中...') : (strings?.searchFiles || '搜索文件/内容')}
                    </Button>
                </Stack>
            </Box>

            <Box sx={{ overflowY: 'auto', flexGrow: 1 }}>
                <List dense sx={{ pt: 0 }}>
                    {files.length === 0 && (
                        <Typography variant="body2" color="text.secondary" sx={{ p: 2 }}>
                            {strings.selectFile}
                        </Typography>
                    )}
                    {files.map((file) => (
                        <ListItemButton
                            key={file.path}
                            onClick={() => handleFileClick(file)}
                            sx={{
                                // Dynamic padding for hierarchy
                                pl: file.depth * 2 + 1,
                                gap: 0.5,
                                alignItems: 'center',
                                position: 'relative',
                                // Hover effect for selection
                                '&:hover': {
                                    backgroundColor: 'action.hover',
                                },
                            }}
                        >
                            {/* Indent lines for better structure */}
                            {file.depth > 0 && Array.from({ length: file.depth - 1 }).map((_, i) => (
                                <Box
                                    key={i}
                                    sx={{
                                        position: 'absolute',
                                        left: (i + 1) * 16 + 8, // Adjust left position
                                        top: 0,
                                        bottom: 0,
                                        width: 1,
                                        bgcolor: 'divider',
                                        opacity: 0.3,
                                    }}
                                />
                            ))}

                            <ListItemIcon sx={{ minWidth: 28, mr: 0, color: 'primary.main' }}>
                                {renderChevron(file)}
                            </ListItemIcon>
                            <ListItemIcon sx={{ minWidth: 28, mr: 0, color: file.kind === 'dir' ? 'primary.light' : 'text.secondary' }}>
                                {file.kind === 'dir' ? <Folder /> : <InsertDriveFile />}
                            </ListItemIcon>
                            <ListItemText
                                primary={file.name}
                                primaryTypographyProps={{ variant: 'body2', noWrap: true }}
                            />
                        </ListItemButton>
                    ))}
                </List>
            </Box>

            {searchResults.length > 0 && (
                <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider', bgcolor: 'background.paper' }}>
                    <Typography variant="subtitle2" sx={{ mb: 1 }}>
                        {strings?.searchFiles || '搜索结果'} ({searchResults.length})
                    </Typography>
                    <Divider sx={{ mb: 1 }} />
                    <List dense sx={{ maxHeight: 200, overflowY: 'auto' }}>
                        {searchResults.map((res) => (
                            <ListItemButton
                                key={res.path + res.jumpToLine}
                                onClick={() => handleFileClick(res)}
                                sx={{ borderBottom: '1px dotted #E0E0E0' }}
                            >
                                <ListItemText
                                    primaryTypographyProps={{ variant: 'caption', fontWeight: 'bold' }}
                                    secondaryTypographyProps={{ variant: 'caption' }}
                                    // 增强: 显示文件名和行号，并使用 snippet 作为辅助文本
                                    primary={
                                        <Stack direction="row" spacing={1} alignItems="center">
                                            <Typography variant="caption" fontWeight="bold">
                                                {res.name}
                                            </Typography>
                                            <Typography variant="caption" color={res.matchType === 'name' ? 'primary.main' : 'text.secondary'}>
                                                {res.jumpToLine > 0 ? `:${res.jumpToLine}` : `(${strings.matchFileName})`}
                                            </Typography>
                                        </Stack>
                                    }
                                    secondary={res.snippet}
                                />
                            </ListItemButton>
                        ))}
                    </List>
                </Box>
            )}
        </Box>
    );
}