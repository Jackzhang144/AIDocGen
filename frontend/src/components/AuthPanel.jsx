import React, { useState } from 'react';
import { Box, Button, TextField, Stack, ToggleButton, ToggleButtonGroup, Typography, Alert } from '@mui/material'; // Added Alert for better error feedback
import axios from 'axios';
import { API_BASE_URL } from '../config';

export default function AuthPanel({ onAuth, strings }) {
    const [mode, setMode] = useState('login');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null); // State for error message

    const submit = async () => {
        setLoading(true);
        setError(null); // Clear previous errors

        if (mode === 'register' && password !== confirm) {
            setError(strings.passwordMismatch || '两次输入的密码不一致');
            setLoading(false);
            return;
        }

        try {
            if (mode === 'login') {
                const res = await axios.post(`${API_BASE_URL}/api/auth/login`, { username, password });
                onAuth(res.data);
            } else {
                const res = await axios.post(`${API_BASE_URL}/api/auth/register`, { username, password, confirmPassword: confirm });
                onAuth(res.data);
            }
        } catch (e) {
            const msg = e.response?.data?.message || e.message;
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box sx={{ p: 2, maxWidth: 360, width: '100%' }}> {/* Slightly smaller max width for focus */}
            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
                <Typography variant="h5" color="primary.main" fontWeight="bold">
                    {strings.appTitle}
                </Typography>
                <ToggleButtonGroup
                    size="small"
                    value={mode}
                    exclusive
                    onChange={(e, val) => {
                        if (val) {
                            setMode(val);
                            setError(null); // Clear error on mode switch
                        }
                    }}
                >
                    <ToggleButton value="login">{strings.login || '登录'}</ToggleButton>
                    <ToggleButton value="register">{strings.register || '注册'}</ToggleButton>
                </ToggleButtonGroup>
            </Stack>
            <Stack spacing={2}>
                {error && <Alert severity="error">{error}</Alert>}
                <TextField
                    label={strings.username || '用户名'}
                    size="small"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    fullWidth
                    variant="outlined"
                />
                <TextField
                    label={strings.password || '密码'}
                    type="password"
                    size="small"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    fullWidth
                    variant="outlined"
                />
                {mode === 'register' && (
                    <TextField
                        label={strings.confirmPassword || '确认密码'}
                        type="password"
                        size="small"
                        value={confirm}
                        onChange={(e) => setConfirm(e.target.value)}
                        fullWidth
                        variant="outlined"
                    />
                )}
                <Button
                    variant="contained"
                    onClick={submit}
                    disabled={loading || (mode === 'register' && password !== confirm)}
                    fullWidth
                    sx={{ mt: 3, py: 1.2 }}
                >
                    {mode === 'login' ? (strings.login || '登录') : (strings.register || '注册')}
                </Button>
            </Stack>
        </Box>
    );
}