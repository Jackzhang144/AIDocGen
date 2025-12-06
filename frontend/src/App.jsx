import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import {
    Box,
    CssBaseline,
    AppBar,
    Toolbar,
    Typography,
    ToggleButton,
    ToggleButtonGroup,
    Stack,
    ThemeProvider,
    createTheme,
    Paper,
    Button
} from '@mui/material';
import Sidebar from './components/Sidebar';
import CodeEditor from './components/CodeEditor';
import AiPanel from './components/AiPanel';
import { LANG, STRINGS } from './i18n';
import AuthPanel from './components/AuthPanel';
import AdminPanel from './components/AdminPanel';
import { API_BASE_URL } from './config';

// 初始宽度和最小/最大限制
const MIN_WIDTH = 250;
const MAX_WIDTH = 600;
const INITIAL_SIDEBAR_WIDTH = 280;
const INITIAL_AIPANEL_WIDTH = 400;

// Define a light, clean theme (保持不变)
const lightTheme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: '#4285F4',
        },
        background: {
            default: '#F5F7FA',
            paper: '#FFFFFF',
        },
    },
    typography: {
        fontFamily: 'system-ui, Avenir, Helvetica, Arial, sans-serif',
    },
    components: {
        MuiAppBar: {
            styleOverrides: {
                root: {
                    backgroundColor: '#FFFFFF',
                    color: '#213547',
                    borderBottom: '1px solid #E0E0E0',
                },
            },
        },
        MuiDrawer: {
            styleOverrides: {
                paper: {
                    backgroundColor: '#F9FBFD',
                    borderRight: '1px solid #E0E0E0',
                },
            },
        },
        MuiButton: {
            defaultProps: {
                disableElevation: true,
            },
            styleOverrides: {
                root: {
                    textTransform: 'none',
                },
            }
        },
        MuiPaper: {
            styleOverrides: {
                root: {
                    boxShadow: '0 1px 3px rgba(0,0,0,0.05), 0 1px 2px rgba(0,0,0,0.1)',
                }
            }
        }
    }
});


export default function App() {
    const [activeFile, setActiveFile] = useState(null);
    const [aiResult, setAiResult] = useState({
        content: null,
        title: 'AI 面板',
        type: null,
        snippetType: null
    });
    const [aiHistory, setAiHistory] = useState([]);
    const [warnings, setWarnings] = useState([]);
    const [usage, setUsage] = useState(null);
    const [, setDirectoryHandle] = useState(null);
    const [lang, setLang] = useState(LANG.zh);
    const [auth, setAuth] = useState(() => {
        const saved = localStorage.getItem('auth');
        return saved ? JSON.parse(saved) : null;
    });
    const [view, setView] = useState('editor');
    const [lastSnippet, setLastSnippet] = useState('');
    const [lastSnippetType, setLastSnippetType] = useState(null);

    // 修复: 引入可变宽度状态
    const [sidebarWidth, setSidebarWidth] = useState(INITIAL_SIDEBAR_WIDTH);
    const [aiPanelWidth, setAiPanelWidth] = useState(INITIAL_AIPANEL_WIDTH);
    const isResizing = useRef(null); // 'left' or 'right'

    const strings = STRINGS[lang];

    // --- 拖拽处理逻辑 ---
    useEffect(() => {
        const handleMouseMove = (e) => {
            if (!isResizing.current) return;

            if (isResizing.current === 'left') {
                const newWidth = e.clientX;
                const clampedWidth = Math.min(Math.max(newWidth, MIN_WIDTH), MAX_WIDTH);
                setSidebarWidth(clampedWidth);
            } else if (isResizing.current === 'right') {
                const windowWidth = window.innerWidth;
                const newWidth = windowWidth - e.clientX;
                const clampedWidth = Math.min(Math.max(newWidth, MIN_WIDTH), MAX_WIDTH);
                setAiPanelWidth(clampedWidth);
            }
        };

        const handleMouseUp = () => {
            if (isResizing.current) {
                isResizing.current = null;
                document.body.style.cursor = 'default';
            }
        };

        window.addEventListener('mousemove', handleMouseMove);
        window.addEventListener('mouseup', handleMouseUp);

        return () => {
            window.removeEventListener('mousemove', handleMouseMove);
            window.removeEventListener('mouseup', handleMouseUp);
        };
    }, []);

    const startResize = (panel) => {
        isResizing.current = panel;
        document.body.style.cursor = 'ew-resize';
    };
    // --- 拖拽处理逻辑结束 ---


    useEffect(() => {
        const savedLang = localStorage.getItem('preferred-lang');
        if (savedLang && (savedLang === LANG.en || savedLang === LANG.zh)) {
            setLang(savedLang);
            return;
        }
        const browser = navigator?.language?.toLowerCase() || '';
        if (browser.startsWith('en')) {
            setLang(LANG.en);
        }
    }, []);

    useEffect(() => {
        localStorage.setItem('preferred-lang', lang);
        setAiResult((prev) => ({ ...prev, title: prev?.title || strings.aiExplain }));
    }, [lang, strings.aiExplain]);

    useEffect(() => {
        if (auth?.token) {
            loadUsage();
        }
    }, [auth]);

    const handleAuth = (data) => {
        const next = { token: data.token, username: data.username, role: data.role };
        setAuth(next);
        localStorage.setItem('auth', JSON.stringify(next));
        setView('editor');
        loadUsage();
    };

    const handleLogout = () => {
        setAuth(null);
        localStorage.removeItem('auth');
        setView('editor');
    };

    const loadUsage = async () => {
        if (!auth?.token) return;
        try {
            const res = await axios.get(`${API_BASE_URL}/api/ai/usage`, {
                headers: { Authorization: `Bearer ${auth.token}` },
            });
            setUsage(res.data);
        } catch (e) {
            console.error(e);
        }
    };

    const handleAiResult = (payload) => {
        let snippetType = null;
        if (payload.snippet) {
            if (payload.type === 'rewrite') {
                snippetType = 'rewritePreview';
            } else if (payload.type === 'comment') {
                snippetType = 'commentPreview';
            } else if (payload.type === 'explain') {
                snippetType = 'explainPreview';
            } else {
                snippetType = 'snippetPreview';
            }
        }

        const entry = {
            ...payload,
            content: payload.content || strings.noContent,
            title: payload.title || strings.aiExplain,
            createdAt: new Date().toISOString(),
            id: Date.now(),
            snippetType: snippetType
        };

        setAiResult(entry);
        setAiHistory((prev) => [entry, ...prev].slice(0, 8));
        setLastSnippet(payload.snippet || '');
        setLastSnippetType(snippetType);

        if (payload.content && auth?.token) {
            axios.post(
                `${API_BASE_URL}/api/ai/safety-check`,
                { content: payload.content },
                { headers: { Authorization: `Bearer ${auth.token}` } }
            ).then((res) => {
                setWarnings(res.data?.warnings || []);
            }).catch(() => setWarnings([]));
        }
        loadUsage();
    };

    const handleSelectHistory = (entry) => {
        setAiResult(entry);
        setLastSnippet(entry.snippet || '');
        setLastSnippetType(entry.snippetType || null);
    };

    return (
        <ThemeProvider theme={lightTheme}>
            <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
                <CssBaseline />

                {/* 顶部导航 */}
                <AppBar position="fixed" elevation={1} sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
                    <Toolbar variant="dense">
                        <Typography variant="h6" noWrap component="div" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                            {strings.appTitle}
                        </Typography>
                        <Box sx={{ flexGrow: 1 }} />
                        {auth ? (
                            <Typography variant="body2" sx={{ mr: 2 }}>
                                {auth.username} ({auth.role})
                            </Typography>
                        ) : null}
                        <ToggleButtonGroup
                            size="small"
                            value={lang}
                            exclusive
                            onChange={(e, val) => val && setLang(val)}
                            sx={{ mr: 1 }}
                        >
                            <ToggleButton value={LANG.zh}>中</ToggleButton>
                            <ToggleButton value={LANG.en}>EN</ToggleButton>
                        </ToggleButtonGroup>
                        {usage ? (
                            <Typography variant="body2" sx={{ mr: 2 }}>
                                {strings.usageInfo}: {usage.todayUsed}/{usage.dailyLimit < 0 ? '∞' : usage.dailyLimit}
                            </Typography>
                        ) : null}
                        {auth ? (
                            <Stack direction="row" spacing={1} alignItems="center">
                                {auth.role === 'ADMIN' && (
                                    <ToggleButtonGroup
                                        size="small"
                                        value={view}
                                        exclusive
                                        onChange={(e, val) => val && setView(val)}
                                        sx={{ mr: 1 }}
                                    >
                                        <ToggleButton value="editor">{strings.editorPage}</ToggleButton>
                                        <ToggleButton value="admin">{strings.adminPage}</ToggleButton>
                                    </ToggleButtonGroup>
                                )}
                                <Button size="small" variant="outlined" onClick={handleLogout}>
                                    {strings.logout}
                                </Button>
                            </Stack>
                        ) : null}
                    </Toolbar>
                </AppBar>

                {!auth ? (
                    // 未登录：仅显示认证页面
                    <Box sx={{ flexGrow: 1, pt: '56px', height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', bgcolor: 'background.default' }}>
                        <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
                            <AuthPanel onAuth={handleAuth} strings={strings} />
                        </Paper>
                    </Box>
                ) : view === 'admin' && auth.role === 'ADMIN' ? (
                    // 管理视图：仅显示用户管理
                    <Box component="main" sx={{ flexGrow: 1, pt: '56px', height: '100%', overflowY: 'auto', bgcolor: 'background.default' }}>
                        <AdminPanel token={auth.token} currentUser={auth} strings={strings} />
                    </Box>
                ) : (
                    // 编辑视图：文件树 + 编辑器 + AI 面板
                    <>
                        {/* 文件树侧边栏 (定宽 + 拖拽手柄) */}
                        <Sidebar
                            width={sidebarWidth}
                            onFileSelect={setActiveFile}
                            setDirectoryHandle={setDirectoryHandle}
                            strings={strings}
                        />
                        {/* 左侧拖拽手柄 */}
                        <Box
                            onMouseDown={() => startResize('left')}
                            sx={{
                                width: '4px',
                                height: '100vh',
                                position: 'fixed',
                                left: sidebarWidth,
                                top: 0,
                                cursor: 'ew-resize',
                                zIndex: 1099,
                                bgcolor: isResizing.current === 'left' ? 'primary.main' : 'transparent',
                                '&:hover': { bgcolor: 'primary.light' },
                            }}
                        />

                        {/* 中间主要编辑器区域 */}
                        <Box
                            component="main"
                            sx={{
                                flexGrow: 1,
                                pt: '56px',
                                height: '100vh', // 确保占满高度
                                minWidth: `calc(100% - ${sidebarWidth + aiPanelWidth}px)`,
                                ml: `${sidebarWidth}px`, // 动态左边距
                                mr: `${aiPanelWidth}px`, // 动态右边距
                                bgcolor: 'background.default',
                                overflow: 'hidden'
                            }}
                        >
                            <CodeEditor
                                activeFile={activeFile}
                                setActiveFile={setActiveFile}
                                onAiResult={handleAiResult}
                                lang={lang}
                                strings={strings}
                                token={auth.token}
                                role={auth.role}
                                usage={usage}
                                onUsageRefresh={loadUsage}
                            />
                        </Box>

                        {/* AI 侧边栏 (定宽 + 拖拽手柄) */}
                        <AiPanel
                            width={aiPanelWidth}
                            content={aiResult?.content || strings.noContent}
                            title={aiResult?.title || strings.aiExplain}
                            warnings={warnings}
                            history={aiHistory}
                            onSelectHistory={handleSelectHistory}
                            currentSnippet={lastSnippet}
                            snippetType={lastSnippetType}
                            createdAt={aiResult?.createdAt}
                            strings={strings}
                        />
                        {/* 右侧拖拽手柄 */}
                        <Box
                            onMouseDown={() => startResize('right')}
                            sx={{
                                width: '4px',
                                height: '100vh',
                                position: 'fixed',
                                right: aiPanelWidth,
                                top: 0,
                                cursor: 'ew-resize',
                                zIndex: 1099,
                                bgcolor: isResizing.current === 'right' ? 'primary.main' : 'transparent',
                                '&:hover': { bgcolor: 'primary.light' },
                            }}
                        />
                    </>
                )}
            </Box>
        </ThemeProvider>
    );
}