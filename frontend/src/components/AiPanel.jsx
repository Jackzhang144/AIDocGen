import React, { useEffect, useState } from 'react';
import { Box, Typography, Divider, Chip, Stack, List, ListItemButton, ListItemText, Tooltip } from '@mui/material';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

// 修复: 接收新的 prop: snippetType
export default function AiPanel({ width, content, onClose, title, warnings = [], history = [], onSelectHistory = () => {}, currentSnippet, createdAt, strings, snippetType }) {
    const [drawerWidth, setDrawerWidth] = useState(width || 480);

    useEffect(() => {
        // 使用外部传入的 width prop，用于动态宽度调整
        if (width) setDrawerWidth(width);
    }, [width]);

    // Custom Markdown Components for a light theme
    const markdownComponents = {
        h1: ({ node, ...props }) => <Typography variant="h5" gutterBottom sx={{ mt: 2 }} {...props} />,
        h2: ({ node, ...props }) => <Typography variant="h6" gutterBottom sx={{ mt: 2 }} {...props} />,
        h3: ({ node, ...props }) => <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }} {...props} />,
        p: ({ node, ...props }) => <Typography variant="body2" paragraph {...props} />,
        li: ({ node, ...props }) => <li style={{ marginBottom: 4 }} {...props} />,
        // Light theme code block styling
        code: ({ inline, className, children, ...props }) => (
            <Box
                component="code"
                sx={{
                    display: inline ? 'inline' : 'block',
                    // Changed from dark to light background for code
                    bgcolor: inline ? '#EFEFEF' : '#F9F9F9',
                    color: 'text.primary',
                    p: inline ? 0.5 : 1,
                    borderRadius: 1,
                    whiteSpace: inline ? 'normal' : 'pre-wrap',
                    fontFamily: 'monospace',
                    border: inline ? 'none' : '1px solid #E0E0E0',
                    fontSize: inline ? '0.8rem' : '0.9rem'
                }}
                className={className}
                {...props}
            >
                {children}
            </Box>
        ),
        // Overrides for pre/code structure, needed for better styling
        pre: ({ node, ...props }) => (
            <Box sx={{ my: 1, p: 0, m: 0 }}>
                {props.children}
            </Box>
        ),
    };

    // 移除了 getSnippetTitle 函数

    return (
        // 修复: 替换 Drawer 为固定定位的 Box (AiPanel)
        <Box
            sx={{
                width: drawerWidth,
                flexShrink: 0,
                position: 'fixed', // 使用固定定位以不影响主内容流
                right: 0,
                top: 0,
                height: '100vh',
                boxSizing: 'border-box',
                pt: '56px',
                zIndex: (theme) => theme.zIndex.drawer, // 确保在主内容之上但在 AppBar 之下
                backgroundColor: 'background.paper', // 匹配主题中的 Drawer/Paper 背景
                borderLeft: '1px solid #E0E0E0',
            }}
        >
            <Box sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'background.paper' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Typography variant="h6" fontWeight="bold">{title || strings.aiExplain}</Typography>
                </Box>
                <Divider sx={{ mb: 2 }} />

                {createdAt && (
                    <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                        {new Date(createdAt).toLocaleString()}
                    </Typography>
                )}

                {warnings?.length ? (
                    <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }}>
                        {warnings.map((w) => <Chip key={w} color="warning" label={w} size="small" />)}
                    </Stack>
                ) : null}

                {/* 修复: 移除代码预览部分，直接显示 AI 得到的回复 */}

                {/* Markdown Content Area */}
                <Box sx={{ overflowY: 'auto', flex: 1, pb: 2 }}>
                    <ReactMarkdown
                        remarkPlugins={[remarkGfm]}
                        components={markdownComponents}
                    >
                        {content}
                    </ReactMarkdown>
                </Box>

                {/* History Section */}
                {history?.length ? (
                    <Box sx={{ borderTop: 1, borderColor: 'divider', pt: 1, mt: 2, maxHeight: 250 }}>
                        <Typography variant="subtitle2" sx={{ mb: 1 }}>{strings?.aiHistory || '历史'}</Typography>
                        <List dense sx={{ overflowY: 'auto', maxHeight: 200, bgcolor: '#F9FBFD', borderRadius: 1 }}>
                            {history.map((item) => (
                                <ListItemButton
                                    key={item.id}
                                    onClick={() => onSelectHistory(item)}
                                    sx={{ borderBottom: '1px solid #EDEDED' }}
                                >
                                    <ListItemText
                                        primary={item.title}
                                        secondary={
                                            <Tooltip title={item.snippet || ''}>
                                                <Typography component="span" variant="caption" color="text.secondary">
                                                    {item.type || ''} · {new Date(item.createdAt).toLocaleTimeString()}
                                                </Typography>
                                            </Tooltip>
                                        }
                                        primaryTypographyProps={{ variant: 'body2', fontWeight: 'medium' }}
                                    />
                                </ListItemButton>
                            ))}
                        </List>
                    </Box>
                ) : null}
            </Box>
        </Box>
    );
}