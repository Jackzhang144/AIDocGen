import React, { useEffect, useState } from 'react';
import { Box, Button, Stack, TextField, MenuItem, Typography, Divider, Grid, Paper, IconButton, CircularProgress, Alert } from '@mui/material';
import { Edit, Delete, PersonAdd } from '@mui/icons-material';
import axios from 'axios';
import { API_BASE_URL } from '../config';

const roles = ['USER', 'MEMBER', 'ADMIN'];

export default function AdminPanel({ token, currentUser, strings }) {
    const [users, setUsers] = useState([]);
    const [form, setForm] = useState({ username: '', password: '', role: 'USER' });
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const authHeader = { headers: { Authorization: `Bearer ${token}` } };

    const loadUsers = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await axios.get(`${API_BASE_URL}/api/admin/users`, authHeader);
            setUsers(res.data);
        } catch (e) {
            console.error(e);
            setError(e.response?.data?.message || e.message || '加载用户失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (token) loadUsers();
    }, [token]);

    const submit = async () => {
        if (!form.username || (!editingId && !form.password)) {
            setError(strings.fieldsRequired || '用户名和密码（新建时）不能为空');
            return;
        }

        setLoading(true);
        setError(null);
        try {
            if (editingId) {
                // Only send password if it's provided, allowing partial update
                const updatePayload = { username: form.username, role: form.role };
                if (form.password) {
                    updatePayload.password = form.password;
                }
                await axios.put(`${API_BASE_URL}/api/admin/users/${editingId}`, updatePayload, authHeader);
            } else {
                await axios.post(`${API_BASE_URL}/api/admin/users`, form, authHeader);
            }
            setForm({ username: '', password: '', role: 'USER' });
            setEditingId(null);
            loadUsers();
        } catch (e) {
            setError(e.response?.data?.message || e.message || '操作失败');
        } finally {
            setLoading(false);
        }
    };

    const editUser = (u) => {
        setEditingId(u.id);
        setForm({ username: u.username, password: '', role: u.role });
        setError(null);
    };

    const deleteUser = async (id, username) => {
        if (!window.confirm(`确认删除用户: ${username}？`)) return;
        setLoading(true);
        setError(null);
        try {
            await axios.delete(`${API_BASE_URL}/api/admin/users/${id}`, authHeader);
            loadUsers();
        } catch (e) {
            setError(e.response?.data?.message || e.message || '删除失败');
        } finally {
            setLoading(false);
        }
    };

    // Helper to format date strings cleanly
    const formatDate = (dateString) => dateString ? dateString.replace('T', ' ').substring(0, 19) : '';


    const headerStyle = { fontWeight: 'bold', color: 'text.secondary', borderBottom: '2px solid', borderColor: 'primary.light', py: 1 };
    const cellStyle = { py: 1, borderBottom: '1px solid', borderColor: 'divider' };

    return (
        <Box sx={{ p: 3, bgcolor: 'background.paper', m: 2, borderRadius: 2, boxShadow: 3 }}>
            <Typography variant="h5" sx={{ mb: 2, fontWeight: 'bold' }}>{strings.adminPanel || '用户管理'}</Typography>

            {/* User Form Section */}
            <Paper elevation={1} sx={{ p: 2, mb: 3, bgcolor: 'grey.50' }}>
                <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 'medium' }}>
                    {editingId ? (strings.update || '更新用户') : (strings.create || '新建用户')}
                </Typography>
                <Stack direction="row" spacing={2} alignItems="center" sx={{ flexWrap: 'wrap' }}>
                    <TextField
                        label={strings.username || '用户名'}
                        size="small"
                        value={form.username}
                        onChange={(e) => setForm({ ...form, username: e.target.value })}
                        disabled={editingId === 1} // Prevent editing admin user's name if ID=1 (a common convention)
                    />
                    <TextField
                        label={editingId ? (strings.newPassword || '新密码 (留空则不修改)') : (strings.password || '密码')}
                        type="password"
                        size="small"
                        value={form.password}
                        onChange={(e) => setForm({ ...form, password: e.target.value })}
                    />
                    <TextField
                        select
                        label={strings.role || '角色'}
                        size="small"
                        value={form.role}
                        onChange={(e) => setForm({ ...form, role: e.target.value })}
                        sx={{ minWidth: 140 }}
                    >
                        {roles.map((r) => <MenuItem key={r} value={r}>{r}</MenuItem>)}
                    </TextField>
                    <Button variant="contained" onClick={submit} disabled={loading} startIcon={editingId ? <Edit /> : <PersonAdd />}>
                        {editingId ? (strings.update || '更新') : (strings.create || '新建')}
                    </Button>
                    {editingId && (
                        <Button
                            variant="outlined"
                            onClick={() => { setEditingId(null); setForm({ username: '', password: '', role: 'USER' }); setError(null); }}
                            disabled={loading}
                        >
                            {strings.cancel || '取消'}
                        </Button>
                    )}
                    {loading && <CircularProgress size={20} />}
                </Stack>
                {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
            </Paper>

            <Divider sx={{ mb: 2 }} />

            {/* User List/Table Section */}
            <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 'medium' }}>{strings.userList || '用户列表'}</Typography>
            <Grid container spacing={0} sx={{ fontSize: 13, border: '1px solid #E0E0E0', borderRadius: 1 }}>
                {/* Table Headers */}
                <Grid item xs={2} sx={{ ...headerStyle, pl: 2 }}>{strings.username || '用户名'}</Grid>
                <Grid item xs={1.5} sx={headerStyle}>{strings.role || '角色'}</Grid>
                <Grid item xs={2.5} sx={headerStyle}>{strings.createdAt || '创建时间'}</Grid>
                <Grid item xs={2.5} sx={headerStyle}>{strings.updatedAt || '更新时间'}</Grid>
                <Grid item xs={1.5} sx={headerStyle}>{strings.apiUsage || 'API用量'}</Grid>
                <Grid item xs={2} sx={headerStyle}>{strings.actions || '操作'}</Grid>

                {/* Table Rows */}
                {users.map((u) => (
                    <React.Fragment key={u.id}>
                        <Grid item xs={2} sx={{ ...cellStyle, pl: 2 }}>
                            <Typography variant="body2">{u.username}</Typography>
                        </Grid>
                        <Grid item xs={1.5} sx={cellStyle}>
                            <Typography variant="body2">{u.role}</Typography>
                        </Grid>
                        <Grid item xs={2.5} sx={cellStyle}>
                            <Typography variant="caption" color="text.secondary">{formatDate(u.createdAt)}</Typography>
                        </Grid>
                        <Grid item xs={2.5} sx={cellStyle}>
                            <Typography variant="caption" color="text.secondary">{formatDate(u.updatedAt)}</Typography>
                        </Grid>
                        <Grid item xs={1.5} sx={cellStyle}>
                            <Typography variant="body2">{u.totalRequests || 0}</Typography>
                        </Grid>
                        <Grid item xs={2} sx={cellStyle}>
                            <Stack direction="row" spacing={0.5} alignItems="center">
                                {u.username === currentUser?.username ? (
                                    <Typography variant="caption" color="primary.main">({strings.current || '当前用户'})</Typography>
                                ) : (
                                    <>
                                        <IconButton size="small" onClick={() => editUser(u)} color="primary" disabled={loading}>
                                            <Edit fontSize="small" />
                                        </IconButton>
                                        <IconButton size="small" color="error" onClick={() => deleteUser(u.id, u.username)} disabled={loading}>
                                            <Delete fontSize="small" />
                                        </IconButton>
                                    </>
                                )}
                            </Stack>
                        </Grid>
                    </React.Fragment>
                ))}
            </Grid>
        </Box>
    );
}