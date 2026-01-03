FROM eclipse-temurin:21-jdk

# Instalar herramientas necesarias
RUN apt-get update && \
    apt-get install -y \
        make \
        openjfx \
        libopenjfx-java \
        xvfb \
        x11vnc \
        fluxbox \
        supervisor \
        wget \
        git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Instalar noVNC
RUN git clone https://github.com/novnc/noVNC.git /opt/novnc && \
    git clone https://github.com/novnc/websockify.git /opt/novnc/utils/websockify && \
    ln -s /opt/novnc/vnc.html /opt/novnc/index.html

ENV DISPLAY=:99

WORKDIR /app

COPY . .

RUN make jar-app

# Configurar supervisor para manejar todos los procesos
RUN mkdir -p /var/log/supervisor && \
    echo '[supervisord]\n\
nodaemon=true\n\
\n\
[program:xvfb]\n\
command=Xvfb :99 -screen 0 1280x720x24\n\
autorestart=true\n\
stdout_logfile=/var/log/supervisor/xvfb.log\n\
stderr_logfile=/var/log/supervisor/xvfb_err.log\n\
\n\
[program:fluxbox]\n\
command=fluxbox\n\
autorestart=true\n\
stdout_logfile=/var/log/supervisor/fluxbox.log\n\
stderr_logfile=/var/log/supervisor/fluxbox_err.log\n\
\n\
[program:x11vnc]\n\
command=x11vnc -display :99 -forever -shared -rfbport 5900\n\
autorestart=true\n\
stdout_logfile=/var/log/supervisor/x11vnc.log\n\
stderr_logfile=/var/log/supervisor/x11vnc_err.log\n\
\n\
[program:novnc]\n\
command=/opt/novnc/utils/novnc_proxy --vnc localhost:5900 --listen 6080\n\
autorestart=true\n\
stdout_logfile=/var/log/supervisor/novnc.log\n\
stderr_logfile=/var/log/supervisor/novnc_err.log\n\
\n\
[program:app]\n\
command=bash -c "sleep 5 && make run-app"\n\
directory=/app\n\
autorestart=false\n\
stdout_logfile=/var/log/supervisor/app.log\n\
stderr_logfile=/var/log/supervisor/app_err.log' > /etc/supervisor/conf.d/supervisord.conf

EXPOSE 6080

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]