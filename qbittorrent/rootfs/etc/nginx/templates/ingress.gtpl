server {
    listen {{ .interface }}:{{ .port }} default_server;

    include /etc/nginx/includes/server_params.conf;
    include /etc/nginx/includes/proxy_params.conf;

    # Ingress hands the panel out inside an iframe belonging to Home Assistant,
    # and parts of the WebUI take for granted that the window above them is
    # qBittorrent's own. That holds for the dialogs, which really are framed by
    # the main window, and breaks for the main window itself, where "parent" is
    # Home Assistant. Each of these throws during startup, and because they run
    # before the interface is built, what is left is a half drawn page.
    #
    # The scripts are compiled into qbittorrent-nox as Qt resources, so there is
    # no file to patch; they are corrected on the way through instead.
    sub_filter_once off;
    sub_filter_types *;

    # color-scheme.js reads the stored colour scheme from the window above.
    # Falling back the way qBittorrent's own dynamicTable.js already does keeps
    # the dialogs on the main window's copy, since none of them load
    # client-data.js and their own is therefore undefined.
    sub_filter
        'const clientData = window.parent.qBittorrent.ClientData;'
        'const clientData = window.qBittorrent.ClientData ?? window.parent.qBittorrent.ClientData;';

    # misc.js resolves the date format the same way, as a default argument.
    sub_filter
        'window.parent.qBittorrent.ClientData.get("date_format")'
        '(window.qBittorrent.ClientData ?? window.parent.qBittorrent.ClientData).get("date_format")';

    # MochaUI sizes its modal underlay from a bare "parent", which is the window
    # itself when nothing frames it. Framed, it lands on Home Assistant, which
    # has no MooTools and therefore no getCoordinates, and the exception stops
    # the rest of the interface from being laid out.
    sub_filter
        'parent.getCoordinates().height'
        'window.getCoordinates().height';

    location / {
        allow   172.30.32.2;
        deny    all;

        # The WebUI addresses everything it asks for relative to the page it
        # was served from, so it lands on the Ingress path on its own and
        # nothing else in the responses needs rewriting.
        proxy_pass {{ .protocol }}://backend;
    }
}
