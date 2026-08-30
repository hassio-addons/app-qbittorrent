server {
    listen {{ .interface }}:{{ .port }} default_server;

    include /etc/nginx/includes/server_params.conf;
    include /etc/nginx/includes/proxy_params.conf;

    location / {
        allow   172.30.32.2;
        deny    all;

        # The WebUI addresses everything it asks for relative to the page it
        # was served from, so it lands on the Ingress path on its own and
        # nothing in the responses needs rewriting.
        proxy_pass {{ .protocol }}://backend;
    }
}
