# AI Doc Gen Frontend

This is the frontend for the AI Code Documentation Generator, built with Vue 3, Vite, Pinia, and Tailwind CSS.

## Project Setup

### Install Dependencies

```bash
npm install
```

### Environment Variables

Create a `.env.local` file in the root of the `frontend2` directory and add the following environment variable if your backend is not running on `http://localhost:8080`:

```
VITE_API_BASE_URL=http://your-api-endpoint
```

By default, the application uses a proxy to forward `/api` requests to `http://localhost:8080`.

### Development Server

To start the development server, run:

```bash
npm run dev
```

### Build for Production

To build the application for production, run:

```bash
npm run build
```
