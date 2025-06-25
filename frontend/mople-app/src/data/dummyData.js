export const dummyData = {
  content: [
    {
      id: 1,
      title: '듄: 파트 투',
      category: 'SF',
      year: 2024,
      rating: 4.7,
      description: '폴 아트레이데스의 신화적 여정이 계속된다.',
      icon: '🎬',
      stats: { likes: 2300, comments: 456, shares: 89 }
    },
    {
      id: 2,
      title: '더 베어 시즌3',
      category: '코미디',
      year: 2024,
      rating: 4.8,
      description: '시카고 샌드위치 가게의 치열한 주방 이야기',
      icon: '📺',
      stats: { likes: 1800, comments: 234, shares: 67 }
    },
    {
      id: 3,
      title: '월드컵 2026 예선',
      category: '스포츠 · 축구',
      year: 2024,
      rating: 4.5,
      description: '한국 vs 일본 아시아 예선 경기',
      icon: '⚽',
      stats: { likes: 3100, comments: 892, shares: 156 }
    }
  ],

  liveRooms: [
    { id: 1, title: '어벤져스: 엔드게임', category: '액션 · 어드벤처', viewers: 127 },
    { id: 2, title: '프리미어리그 맨시티 vs 리버풀', category: '스포츠 · 축구', viewers: 89 },
    { id: 3, title: '오징어 게임 시즌2', category: '드라마 · 스릴러', viewers: 203 }
  ],

  playlists: [
    {
      id: 1,
      title: '주말 영화 모음',
      itemCount: 12,
      items: [
        { id: 1, title: '탑건: 매버릭', category: '액션', duration: '2시간 11분', icon: '🎬' },
        { id: 2, title: '라라랜드', category: '뮤지컬', duration: '2시간 8분', icon: '🎭' },
        { id: 3, title: '인터스텔라', category: 'SF', duration: '2시간 49분', icon: '🎬' }
      ]
    },
    {
      id: 2,
      title: 'K-드라마 베스트',
      itemCount: 8,
      items: [
        { id: 4, title: '오징어 게임', category: '스릴러', duration: '9화', icon: '📺' },
        { id: 5, title: '사랑의 불시착', category: '로맨스', duration: '16화', icon: '📺' },
        { id: 6, title: '킹덤', category: '호러', duration: '12화', icon: '📺' }
      ]
    },
    {
      id: 3,
      title: '스포츠 하이라이트',
      itemCount: 15,
      items: [
        { id: 7, title: 'EPL 베스트 골', category: '축구', duration: '45분', icon: '⚽' },
        { id: 8, title: 'NBA 플레이오프', category: '농구', duration: '2시간', icon: '🏀' },
        { id: 9, title: 'WBC 한국 경기', category: '야구', duration: '3시간', icon: '⚾' }
      ]
    }
  ],

  notifications: [
    {
      id: 1,
      title: '새로운 좋아요',
      message: '이친구님이 회원님의 플레이리스트 "주말 영화 모음"을 좋아합니다.',
      time: '5분 전',
      read: false,
      type: 'like'
    },
    {
      id: 2,
      title: '라이브 룸 알림',
      message: '관심있는 영화 "듄: 파트 투"의 라이브 룸이 시작되었습니다.',
      time: '10분 전',
      read: false,
      type: 'live'
    },
    {
      id: 3,
      title: '댓글 알림',
      message: '박동료님이 회원님의 콘텐츠 "어벤져스: 엔드게임"에 댓글을 남겼습니다.',
      time: '30분 전',
      read: true,
      type: 'comment'
    },
    {
      id: 4,
      title: '팔로우 알림',
      message: '최친구님이 회원님을 팔로우하기 시작했습니다.',
      time: '1시간 전',
      read: false,
      type: 'follow'
    }
  ],

  dmContacts: [
    {
      id: 1,
      name: '이친구',
      avatar: '이',
      status: '온라인',
      lastSeen: '방금 전'
    },
    {
      id: 2,
      name: '박동료',
      avatar: '박',
      status: '5분 전 접속',
      lastSeen: '5분 전'
    },
    {
      id: 3,
      name: '최친구',
      avatar: '최',
      status: '오프라인',
      lastSeen: '1시간 전'
    },
    {
      id: 4,
      name: '김영화',
      avatar: '김',
      status: '온라인',
      lastSeen: '방금 전'
    },
    {
      id: 5,
      name: '정드라마',
      avatar: '정',
      status: '30분 전 접속',
      lastSeen: '30분 전'
    }
  ]
};

// 큐레이션 데이터 - 4개 섹션으로 수정
export const curationData = [
  {
    id: 'high-rated',
    title: '⭐ 평점이 높은',
    category: 'high-rated',
    items: [
      {
        id: 101,
        title: '반지의 제왕',
        year: 2001,
        rating: 4.9,
        icon: '💍',
        genres: ['fantasy'],
        displayGenres: ['판타지', '어드벤처']
      },
      {
        id: 102,
        title: '어벤져스: 엔드게임',
        year: 2019,
        rating: 4.9,
        icon: '🦸',
        genres: ['action', 'sf'],
        displayGenres: ['액션', 'SF']
      },
      {
        id: 103,
        title: '센과 치히로의 행방불명',
        year: 2001,
        rating: 4.9,
        icon: '🐲',
        genres: ['animation'],
        displayGenres: ['애니메이션', '판타지']
      },
      {
        id: 104,
        title: '인터스텔라',
        year: 2014,
        rating: 4.8,
        icon: '🌌',
        genres: ['sf'],
        displayGenres: ['SF', '드라마']
      },
      {
        id: 105,
        title: '더 베어',
        year: 2022,
        rating: 4.8,
        icon: '👨‍🍳',
        genres: ['comedy'],
        displayGenres: ['코미디', '드라마']
      },
      {
        id: 106,
        title: '탑건: 매버릭',
        year: 2022,
        rating: 4.8,
        icon: '✈️',
        genres: ['action'],
        displayGenres: ['액션', '드라마']
      }
    ]
  },
  {
    id: 'movies',
    title: '🎬 영화',
    category: 'movies',
    items: [
      {
        id: 201,
        title: '듄: 파트 투',
        year: 2024,
        rating: 4.7,
        icon: '🏜️',
        genres: ['sf'],
        displayGenres: ['SF', '어드벤처']
      },
      {
        id: 202,
        title: '존 윅 4',
        year: 2023,
        rating: 4.6,
        icon: '🔫',
        genres: ['action'],
        displayGenres: ['액션', '스릴러']
      },
      {
        id: 203,
        title: '라라랜드',
        year: 2016,
        rating: 4.6,
        icon: '🎭',
        genres: ['romance'],
        displayGenres: ['로맨스', '뮤지컬']
      },
      {
        id: 204,
        title: '매드맥스: 분노의 도로',
        year: 2015,
        rating: 4.5,
        icon: '🚗',
        genres: ['action'],
        displayGenres: ['액션', '어드벤처']
      },
      {
        id: 205,
        title: '코코',
        year: 2017,
        rating: 4.7,
        icon: '💀',
        genres: ['animation'],
        displayGenres: ['애니메이션', '가족']
      },
      {
        id: 206,
        title: '블레이드 러너 2049',
        year: 2017,
        rating: 4.6,
        icon: '🤖',
        genres: ['sf'],
        displayGenres: ['SF', '스릴러']
      }
    ]
  },
  {
    id: 'dramas',
    title: '📺 드라마',
    category: 'dramas',
    items: [
      {
        id: 301,
        title: '오징어 게임',
        year: 2021,
        rating: 4.8,
        icon: '🎯',
        genres: ['thriller'],
        displayGenres: ['스릴러', '드라마']
      },
      {
        id: 302,
        title: '사랑의 불시착',
        year: 2019,
        rating: 4.7,
        icon: '🪂',
        genres: ['romance'],
        displayGenres: ['로맨스', '드라마']
      },
      {
        id: 303,
        title: '킹덤',
        year: 2019,
        rating: 4.6,
        icon: '👑',
        genres: ['horror'],
        displayGenres: ['호러', '스릴러']
      },
      {
        id: 304,
        title: '브레이킹 배드',
        year: 2008,
        rating: 4.8,
        icon: '🧪',
        genres: ['thriller'],
        displayGenres: ['스릴러', '범죄']
      },
      {
        id: 305,
        title: '이상한 나라의 앨리스',
        year: 2022,
        rating: 4.5,
        icon: '🃏',
        genres: ['thriller'],
        displayGenres: ['스릴러', '미스터리']
      },
      {
        id: 306,
        title: '브루클린 나인나인',
        year: 2013,
        rating: 4.5,
        icon: '👮',
        genres: ['comedy'],
        displayGenres: ['코미디', '경찰']
      }
    ]
  },
  {
    id: 'sports',
    title: '⚽ 스포츠',
    category: 'sports',
    items: [
      {
        id: 401,
        title: '월드컵 2026 예선',
        year: 2024,
        rating: 4.5,
        icon: '⚽',
        genres: ['sports'],
        displayGenres: ['축구', '국가대표']
      },
      {
        id: 402,
        title: 'EPL 베스트 골',
        year: 2024,
        rating: 4.3,
        icon: '⚽',
        genres: ['sports'],
        displayGenres: ['축구', '프리미어리그']
      },
      {
        id: 403,
        title: 'NBA 플레이오프',
        year: 2024,
        rating: 4.4,
        icon: '🏀',
        genres: ['sports'],
        displayGenres: ['농구', 'NBA']
      },
      {
        id: 404,
        title: 'WBC 한국 경기',
        year: 2023,
        rating: 4.6,
        icon: '⚾',
        genres: ['sports'],
        displayGenres: ['야구', '국가대표']
      },
      {
        id: 405,
        title: '파리 올림픽 하이라이트',
        year: 2024,
        rating: 4.5,
        icon: '🏅',
        genres: ['sports'],
        displayGenres: ['올림픽', '종합']
      },
      {
        id: 406,
        title: 'UFC 파이트 나이트',
        year: 2024,
        rating: 4.2,
        icon: '🥊',
        genres: ['sports'],
        displayGenres: ['격투기', 'UFC']
      }
    ]
  }
];