FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# Nori 플러그인 설치
RUN bin/elasticsearch-plugin install --batch analysis-nori

# 실행 권한 부여
USER elasticsearch
