window.onload = function() {
    window.ui = SwaggerUIBundle({
        url: "/v3/api-docs",   // ✅ 자동으로 이 URL을 로드
        dom_id: '#swagger-ui',
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        layout: "BaseLayout"
    });
};
