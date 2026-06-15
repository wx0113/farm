-- ============================================================
-- Seedsystem 农场管理系统 数据库导出
-- 导出日期: 2026-06-11 10:42:25
-- 数据库: farm_db (PostgreSQL)
-- ============================================================

--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: farm_land; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.farm_land (
    id bigint NOT NULL,
    current_season integer,
    current_stage integer,
    has_worm boolean,
    land_index integer NOT NULL,
    land_type character varying(20),
    plant_time timestamp(6) without time zone,
    player_id bigint NOT NULL,
    seed_id integer,
    stage_start_time timestamp(6) without time zone,
    withered boolean,
    yield_reduction integer
);


ALTER TABLE public.farm_land OWNER TO postgres;

--
-- Name: farm_land_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.farm_land_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.farm_land_id_seq OWNER TO postgres;

--
-- Name: farm_land_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.farm_land_id_seq OWNED BY public.farm_land.id;


--
-- Name: growth_stage; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.growth_stage (
    id integer NOT NULL,
    seed_id integer NOT NULL,
    stage_order integer NOT NULL,
    stage_title character varying(50) NOT NULL,
    stage_duration integer DEFAULT 0,
    pest_probability numeric(38,2) DEFAULT 0.00,
    image_url character varying(255),
    image_width integer DEFAULT 97,
    image_height integer DEFAULT 136,
    image_offset_x integer DEFAULT 53,
    image_offset_y integer DEFAULT 151,
    crop_status character varying(20) DEFAULT '正常'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    crop_image character varying(255)
);


ALTER TABLE public.growth_stage OWNER TO postgres;

--
-- Name: growth_stage_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.growth_stage_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.growth_stage_id_seq OWNER TO postgres;

--
-- Name: growth_stage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.growth_stage_id_seq OWNED BY public.growth_stage.id;


--
-- Name: player; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.player (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    nickname character varying(50),
    exp integer DEFAULT 0,
    points integer DEFAULT 0,
    gold integer DEFAULT 0,
    avatar_url character varying(255),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.player OWNER TO postgres;

--
-- Name: player_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.player_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.player_id_seq OWNER TO postgres;

--
-- Name: player_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.player_id_seq OWNED BY public.player.id;


--
-- Name: player_seed; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.player_seed (
    id bigint NOT NULL,
    create_time timestamp(6) without time zone,
    player_id bigint NOT NULL,
    quantity integer NOT NULL,
    seed_id integer NOT NULL
);


ALTER TABLE public.player_seed OWNER TO postgres;

--
-- Name: player_seed_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.player_seed_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.player_seed_id_seq OWNER TO postgres;

--
-- Name: player_seed_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.player_seed_id_seq OWNED BY public.player_seed.id;


--
-- Name: seed; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.seed (
    id integer NOT NULL,
    seed_id character varying(20) NOT NULL,
    seed_name character varying(50) NOT NULL,
    x_season_crop character varying(20) DEFAULT '1季作物'::character varying,
    seed_level integer DEFAULT 1,
    seed_type character varying(20) DEFAULT '蔬菜'::character varying,
    experience integer DEFAULT 0,
    maturity_time integer DEFAULT 0,
    harvest_count integer DEFAULT 0,
    purchase_price numeric(38,2) DEFAULT 0.00,
    fruit_price numeric(38,2) DEFAULT 0.00,
    land_requirement character varying(20) DEFAULT '黄土地'::character varying,
    points integer DEFAULT 0,
    tip_info text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.seed OWNER TO postgres;

--
-- Name: seed_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seed_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seed_id_seq OWNER TO postgres;

--
-- Name: seed_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.seed_id_seq OWNED BY public.seed.id;


--
-- Name: farm_land id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.farm_land ALTER COLUMN id SET DEFAULT nextval('public.farm_land_id_seq'::regclass);


--
-- Name: growth_stage id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.growth_stage ALTER COLUMN id SET DEFAULT nextval('public.growth_stage_id_seq'::regclass);


--
-- Name: player id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.player ALTER COLUMN id SET DEFAULT nextval('public.player_id_seq'::regclass);


--
-- Name: player_seed id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.player_seed ALTER COLUMN id SET DEFAULT nextval('public.player_seed_id_seq'::regclass);


--
-- Name: seed id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seed ALTER COLUMN id SET DEFAULT nextval('public.seed_id_seq'::regclass);


--
-- Name: farm_land farm_land_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.farm_land
    ADD CONSTRAINT farm_land_pkey PRIMARY KEY (id);


--
-- Name: growth_stage growth_stage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.growth_stage
    ADD CONSTRAINT growth_stage_pkey PRIMARY KEY (id);


--
-- Name: player player_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.player
    ADD CONSTRAINT player_pkey PRIMARY KEY (id);


--
-- Name: player_seed player_seed_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.player_seed
    ADD CONSTRAINT player_seed_pkey PRIMARY KEY (id);


--
-- Name: player player_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.player
    ADD CONSTRAINT player_username_key UNIQUE (username);


--
-- Name: seed seed_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seed
    ADD CONSTRAINT seed_pkey PRIMARY KEY (id);


--
-- Name: seed seed_seed_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seed
    ADD CONSTRAINT seed_seed_id_key UNIQUE (seed_id);


--
-- Name: player_seed ukfogk56nfys4v7o3cr2wnaokkm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.player_seed
    ADD CONSTRAINT ukfogk56nfys4v7o3cr2wnaokkm UNIQUE (player_id, seed_id);


--
-- Name: growth_stage growth_stage_seed_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.growth_stage
    ADD CONSTRAINT growth_stage_seed_id_fkey FOREIGN KEY (seed_id) REFERENCES public.seed(id);


--
-- PostgreSQL database dump complete
--


-- ============================================================
-- 数据
-- ============================================================

--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: farm_land; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.farm_land (id, current_season, current_stage, has_worm, land_index, land_type, plant_time, player_id, seed_id, stage_start_time, withered, yield_reduction) FROM stdin;
7	\N	\N	f	6	黑土地	\N	1	\N	\N	f	0
8	\N	\N	f	7	黑土地	\N	1	\N	\N	f	0
12	\N	\N	f	11	黑土地	\N	1	\N	\N	f	0
13	\N	\N	f	12	沙土地	\N	1	\N	\N	f	0
14	\N	\N	f	13	沙土地	\N	1	\N	\N	f	0
19	\N	\N	f	18	沙土地	\N	1	\N	\N	f	0
20	\N	\N	f	19	沙土地	\N	1	\N	\N	f	0
21	\N	\N	f	20	沙土地	\N	1	\N	\N	f	0
22	\N	\N	f	21	沙土地	\N	1	\N	\N	f	0
23	\N	\N	f	22	沙土地	\N	1	\N	\N	f	0
24	\N	\N	f	23	沙土地	\N	1	\N	\N	f	0
83	1	-1	f	10	黑土地	2026-06-01 15:15:22.247097	2	6	2026-06-01 15:18:39.648971	f	0
2	\N	\N	f	1	黄土地	\N	1	\N	\N	f	0
1	\N	\N	f	0	黄土地	\N	1	\N	\N	f	0
33	\N	\N	f	8	黑土地	\N	3	\N	\N	f	0
88	1	-1	f	15	沙土地	2026-06-01 14:50:28.01926	2	8	2026-06-01 14:53:34.63753	f	0
76	\N	\N	f	3	黄土地	\N	2	\N	\N	f	0
11	1	-1	f	10	黑土地	2026-06-01 13:35:52.316697	1	6	2026-06-01 13:39:09.107813	f	0
75	\N	\N	f	2	黄土地	\N	2	\N	\N	f	0
5	\N	\N	f	4	黄土地	\N	1	\N	\N	f	0
6	\N	\N	f	5	黄土地	\N	1	\N	\N	f	0
78	1	-1	f	5	黄土地	2026-06-01 14:44:09.840068	2	1	2026-06-01 14:46:34.34975	t	0
3	1	-1	f	2	黄土地	2026-06-01 13:58:11.139619	1	1	2026-06-01 14:00:37.979468	f	0
73	\N	\N	f	0	黄土地	\N	2	\N	\N	f	0
74	\N	\N	f	1	黄土地	\N	2	\N	\N	f	0
79	\N	\N	f	6	黑土地	\N	2	\N	\N	f	0
80	\N	\N	f	7	黑土地	\N	2	\N	\N	f	0
16	\N	\N	f	15	沙土地	\N	1	\N	\N	f	0
15	\N	\N	f	14	沙土地	\N	1	\N	\N	f	0
84	\N	\N	f	11	黑土地	\N	2	\N	\N	f	0
85	\N	\N	f	12	沙土地	\N	2	\N	\N	f	0
86	\N	\N	f	13	沙土地	\N	2	\N	\N	f	0
4	1	-1	f	3	黄土地	2026-06-01 13:18:06.336166	1	1	2026-06-01 13:20:35.110462	f	1
89	\N	\N	f	16	沙土地	\N	2	\N	\N	f	0
90	\N	\N	f	17	沙土地	\N	2	\N	\N	f	0
91	\N	\N	f	18	沙土地	\N	2	\N	\N	f	0
92	\N	\N	f	19	沙土地	\N	2	\N	\N	f	0
93	\N	\N	f	20	沙土地	\N	2	\N	\N	f	0
94	\N	\N	f	21	沙土地	\N	2	\N	\N	f	0
95	\N	\N	f	22	沙土地	\N	2	\N	\N	f	0
96	\N	\N	f	23	沙土地	\N	2	\N	\N	f	0
17	\N	\N	f	16	沙土地	\N	1	\N	\N	f	0
18	\N	\N	f	17	沙土地	\N	1	\N	\N	f	0
81	\N	\N	f	8	黑土地	\N	2	\N	\N	f	0
25	\N	\N	f	0	黄土地	\N	3	\N	\N	f	0
82	\N	\N	f	9	黑土地	\N	2	\N	\N	f	0
9	\N	\N	f	8	黑土地	\N	1	\N	\N	f	0
10	\N	\N	f	9	黑土地	\N	1	\N	\N	f	0
31	\N	\N	f	6	黑土地	\N	3	\N	\N	f	0
32	\N	\N	f	7	黑土地	\N	3	\N	\N	f	0
35	\N	\N	f	10	黑土地	\N	3	\N	\N	f	0
36	\N	\N	f	11	黑土地	\N	3	\N	\N	f	0
26	\N	\N	f	1	黄土地	\N	3	\N	\N	f	0
34	\N	\N	f	9	黑土地	\N	3	\N	\N	f	0
87	\N	\N	f	14	沙土地	\N	2	\N	\N	f	0
29	\N	\N	f	4	黄土地	\N	3	\N	\N	f	0
77	1	-1	f	4	黄土地	2026-06-01 15:14:04.682429	2	1	2026-06-01 15:16:31.662872	f	0
28	\N	\N	f	3	黄土地	\N	3	\N	\N	f	0
27	\N	\N	f	2	黄土地	\N	3	\N	\N	f	0
30	\N	\N	f	5	黄土地	\N	3	\N	\N	f	0
43	\N	\N	f	18	沙土地	\N	3	\N	\N	f	0
44	\N	\N	f	19	沙土地	\N	3	\N	\N	f	0
45	\N	\N	f	20	沙土地	\N	3	\N	\N	f	0
46	\N	\N	f	21	沙土地	\N	3	\N	\N	f	0
47	\N	\N	f	22	沙土地	\N	3	\N	\N	f	0
48	\N	\N	f	23	沙土地	\N	3	\N	\N	f	0
37	\N	\N	f	12	金土地	\N	3	\N	\N	f	0
38	\N	\N	f	13	金土地	\N	3	\N	\N	f	0
41	\N	\N	f	16	金土地	\N	3	\N	\N	f	0
42	\N	\N	f	17	金土地	\N	3	\N	\N	f	0
40	\N	\N	f	15	金土地	\N	3	\N	\N	f	0
57	2	-1	f	8	黑土地	2026-06-01 00:19:53.622795	4	6	2026-06-09 22:49:26.473934	f	0
53	1	-1	f	4	黄土地	2026-06-09 22:47:04.762648	4	1	2026-06-09 22:49:30.486927	f	0
60	1	-1	f	11	黑土地	2026-06-09 22:46:33.893992	4	6	2026-06-09 22:49:50.475638	f	0
49	\N	\N	f	0	黄土地	\N	4	\N	\N	f	0
50	\N	\N	f	1	黄土地	\N	4	\N	\N	f	0
52	\N	\N	f	3	黄土地	\N	4	\N	\N	f	0
54	\N	\N	f	5	黄土地	\N	4	\N	\N	f	0
55	\N	\N	f	6	黑土地	\N	4	\N	\N	f	0
56	\N	\N	f	7	黑土地	\N	4	\N	\N	f	0
58	\N	\N	f	9	黑土地	\N	4	\N	\N	f	0
59	\N	\N	f	10	黑土地	\N	4	\N	\N	f	0
67	\N	\N	f	18	沙土地	\N	4	\N	\N	f	0
68	\N	\N	f	19	沙土地	\N	4	\N	\N	f	0
69	\N	\N	f	20	沙土地	\N	4	\N	\N	f	0
70	\N	\N	f	21	沙土地	\N	4	\N	\N	f	0
71	\N	\N	f	22	沙土地	\N	4	\N	\N	f	0
72	\N	\N	f	23	沙土地	\N	4	\N	\N	f	0
39	\N	\N	f	14	金土地	\N	3	\N	\N	f	0
61	\N	\N	f	12	金土地	\N	4	\N	\N	f	0
62	\N	\N	f	13	金土地	\N	4	\N	\N	f	0
63	\N	\N	f	14	金土地	\N	4	\N	\N	f	0
64	\N	\N	f	15	金土地	\N	4	\N	\N	f	0
65	\N	\N	f	16	金土地	\N	4	\N	\N	f	0
66	\N	\N	f	17	金土地	\N	4	\N	\N	f	0
51	\N	\N	f	2	黄土地	\N	4	\N	\N	f	0
\.


--
-- Data for Name: seed; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.seed (id, seed_id, seed_name, x_season_crop, seed_level, seed_type, experience, maturity_time, harvest_count, purchase_price, fruit_price, land_requirement, points, tip_info, created_at, updated_at) FROM stdin;
8	S008	豌豆	1季作物	2	蔬菜	18	280	5	45.00	12.00	沙土地	8	🫛 翠绿的豌豆，小巧玲珑。生长周期短，成熟快，适合新手练手。虽然单价不高，但生长速度能让你快速积累经验。	2026-05-18 08:20:07.312732	2026-06-01 14:06:54.666394
9	S009	辣椒	2季作物	4	蔬菜	35	380	4	90.00	30.00	沙土地	18	🌶️ 火辣辣的小辣椒！只长在沙土地上，两季收获。成熟后每个能卖30金币，利润可观。注意虫害概率较高，勤除虫哦！	2026-05-18 08:20:07.312732	2026-05-28 00:44:08.62188
13	S002	葡萄	2季作物	5	水果	45	450	3	120.00	50.00	沙土地	25	🍇 晶莹剔透的葡萄串，沙土地上的贵族水果。果实单价高达50金币，两季收入可观。耐心等待成熟，回报丰厚！	2026-05-25 15:48:18.731163	2026-05-28 13:37:39.349709
1	S001	草莓	1季作物	2	水果	20	100	5	50.00	15.00	黄土地	10	🍓 草莓是新手最爱的水果！红润多汁，种在黄土地上生长飞快。成熟后每颗可卖15金币，是前期攒钱的不二之选。	2026-05-25 16:09:56.400931	2026-06-08 15:54:09.886481
14	S003	西瓜	1季作物	4	水果	40	500	2	100.00	60.00	黄土地	20	🍉 夏天必备的大西瓜！生长周期较长但果实巨大，每个卖60金币。虽然单季单次收获量不多，但单价高适合长期投资。	2026-05-25 15:48:24.95587	2026-05-25 15:48:24.95587
30	S004	星星果	3季作物	8	水果	60	600	3	250.00	100.00	黑土地	35	⭐ 传说中的星星果！黑土地专属高级作物，三季收获让你一次投入长期受益。果实价值100金币，是中后期发家致富的利器！	2026-05-18 08:20:07.312732	2026-05-18 08:20:07.312732
32	S005	钻石果	3季作物	10	水果	80	700	1	500.00	300.00	沙土地	50	💎 农场中最珍贵的钻石果！沙土地顶级作物，三季生长，每季结出一颗价值300金币的果实。投资大但回报惊人，适合老玩家挑战！	2026-05-18 08:20:07.312732	2026-05-18 08:20:07.312732
418	S011	玉米	1季作物	1	谷物	10	200	6	30.00	8.00	黄土地	5	🌽 金灿灿的玉米棒子！最基础的谷物作物，黄土地上快速生长。产量多、周期短，是新手熟悉农场操作的最佳选择。	2026-05-18 08:20:07.312732	2026-05-18 08:20:07.312732
933	S012	白萝卜	1季作物	1	蔬菜	8	180	8	25.00	6.00	黄土地	4	🥬 白胖胖的大萝卜！生长速度极快，180秒就能成熟，一次收获8个。虽然单价低，但胜在薄利多收，适合快速刷经验。	2026-05-18 08:20:07.312732	2026-05-18 08:20:07.312732
6	S006	茄子	2季作物	3	蔬菜	30	400	4	80.00	25.00	黑土地	15	🍆 紫皮长茄，黑土地上的明星作物！两季收获，果实饱满。记得及时除虫，否则产量会大打折扣哦~	2026-05-18 08:20:07.312732	2026-06-01 13:57:26.509118
\.


--
-- Data for Name: growth_stage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.growth_stage (id, seed_id, stage_order, stage_title, stage_duration, pest_probability, image_url, image_width, image_height, image_offset_x, image_offset_y, crop_status, created_at, updated_at, crop_image) FROM stdin;
34	6	4	膨大阶段	40	0.10	/images/crops/6/4.png	203	278	35	-94	正常	2026-06-08 16:29:04.736953	2026-06-08 16:29:04.736953	\N
35	6	5	成熟阶段	10	0.05	/images/crops/6/5.png	179	257	43	-72	正常	2026-06-08 16:29:15.815715	2026-06-08 16:29:15.815715	\N
46	8	4	开花阶段	40	0.12	/images/crops/8/4.png	220	220	110	110	正常	2026-05-18 08:27:44.065462	2026-05-18 08:27:44.065462	\N
47	8	5	成熟阶段	10	0.05	/images/crops/8/5.png	220	220	110	110	正常	2026-05-18 08:27:44.065462	2026-05-18 08:27:44.065462	\N
11	13	5	成熟阶段	10	0.05	/images/crops/13/5.png	216	216	38	-56	正常	2026-06-08 17:08:21.91872	2026-06-08 17:08:21.91872	\N
45	8	3	生长阶段	50	0.15	/images/crops/8/3.png	422	422	70	70	正常	2026-05-28 21:30:30.986654	2026-05-28 21:30:30.986654	/crops/c2cb8cb8-1700-4370-8f4b-dcc72d0a0d50.png
1	1	1	种子阶段	30	0.05	/images/crops/basic/0.png	112	132	87	32	正常	2026-06-08 14:46:25.447847	2026-06-08 14:46:25.447847	\N
2	1	2	发芽阶段	40	0.10	/images/crops/1/2.png	178	217	45	-32	正常	2026-06-08 14:46:29.859073	2026-06-08 14:46:29.859073	\N
43	8	1	种子阶段	35	0.05	/images/crops/basic/0.png	282	282	88	70	正常	2026-05-18 08:27:44.065462	2026-05-18 08:27:44.065462	\N
3	1	3	生长阶段	35	0.15	/images/crops/1/3.png	223	246	28	-86	正常	2026-06-08 14:46:35.751569	2026-06-08 14:46:35.751569	\N
44	8	2	发芽阶段	45	0.10	/images/crops/8/2.png	352	352	66	70	正常	2026-05-18 08:27:44.065462	2026-05-18 08:27:44.065462	\N
49	9	1	种子阶段	50	0.05	/images/crops/basic/0.png	282	282	88	70	正常	2026-05-18 08:27:44.122924	2026-05-18 08:27:44.122924	\N
50	9	2	发芽阶段	70	0.10	/images/crops/9/2.png	352	352	66	70	正常	2026-05-18 08:27:44.122924	2026-05-18 08:27:44.122924	\N
51	9	3	生长阶段	80	0.15	/images/crops/9/3.png	440	440	44	70	正常	2026-05-18 08:27:44.122924	2026-05-18 08:27:44.122924	\N
7	13	1	种子阶段	40	0.05	/images/crops/basic/0.png	282	282	88	70	正常	2026-05-18 08:27:43.762525	2026-05-18 08:27:43.762525	\N
4	1	4	开花阶段	25	0.10	/images/crops/1/4.png	258	240	6	-90	正常	2026-06-08 14:32:07.595839	2026-06-08 14:32:07.595839	\N
8	13	2	发芽阶段	50	0.10	/images/crops/13/2.png	352	352	66	70	正常	2026-05-18 08:27:43.762525	2026-05-18 08:27:43.762525	\N
9	13	3	生长阶段	45	0.15	/images/crops/13/3.png	440	440	44	70	正常	2026-05-18 08:27:43.762525	2026-05-18 08:27:43.762525	\N
5	1	5	成熟阶段	10	0.05	/images/crops/1/5.png	269	257	18	-111	正常	2026-06-08 14:32:14.194053	2026-06-08 14:32:14.194053	\N
31	6	1	种子阶段	40	0.05	/images/crops/basic/0.png	106	123	86	24	正常	2026-06-08 16:28:38.625932	2026-06-08 16:28:38.625932	\N
32	6	2	发芽阶段	50	0.08	/images/crops/6/2.png	211	243	28	-38	正常	2026-06-08 16:28:50.727608	2026-06-08 16:28:50.727608	\N
33	6	3	生长阶段	50	0.12	/images/crops/6/3.png	221	265	23	-86	正常	2026-06-08 16:28:57.103426	2026-06-08 16:28:57.103426	\N
52	9	4	抽穗阶段	70	0.12	/images/crops/9/4.png	220	220	110	110	正常	2026-05-18 08:27:44.122924	2026-05-18 08:27:44.122924	\N
53	9	5	成熟阶段	10	0.05	/images/crops/9/5.png	220	220	110	110	正常	2026-05-18 08:27:44.122924	2026-05-18 08:27:44.122924	\N
10	13	4	开花阶段	35	0.12	/images/crops/13/4.png	220	220	110	110	正常	2026-05-18 08:27:43.762525	2026-05-18 08:27:43.762525	\N
19	30	1	种子阶段	50	0.05	/images/crops/basic/0.png	282	282	88	70	正常	2026-05-18 08:27:43.824112	2026-05-18 08:27:43.824112	\N
20	30	2	发芽阶段	60	0.10	/images/crops/30/2.png	352	352	66	70	正常	2026-05-18 08:27:43.824112	2026-05-18 08:27:43.824112	\N
21	30	3	生长阶段	70	0.15	/images/crops/30/3.png	440	440	44	70	正常	2026-05-18 08:27:43.824112	2026-05-18 08:27:43.824112	\N
68	933	2	发芽阶段	50	0.10	/images/crops/933/2.png	352	352	66	70	正常	2026-05-18 08:27:44.263901	2026-05-18 08:27:44.263901	\N
80	32	6	枯草	0	0.00	/images/crops/basic/9.png	216	216	51	-19	枯萎	2026-06-08 17:09:04.465878	2026-06-08 17:09:04.465878	\N
22	30	4	抽穗阶段	60	0.12	/images/crops/30/4.png	220	220	110	110	正常	2026-05-18 08:27:43.824112	2026-05-18 08:27:43.824112	\N
65	418	5	成熟阶段	10	0.05	/images/crops/418/5.png	216	216	60	-107	正常	2026-06-08 17:09:12.370408	2026-06-08 17:09:12.370408	\N
64	418	4	开花阶段	70	0.12	/images/crops/418/4.png	220	220	110	110	正常	2026-05-18 08:27:44.200984	2026-05-18 08:27:44.200984	\N
25	32	1	种子阶段	60	0.05	/images/crops/basic/0.png	282	282	88	70	正常	2026-05-18 08:27:43.881125	2026-05-18 08:27:43.881125	\N
81	418	6	枯草	0	0.00	/images/crops/basic/9.png	216	216	53	3	枯萎	2026-06-08 17:09:17.145331	2026-06-08 17:09:17.145331	\N
71	933	5	成熟阶段	10	0.05	/images/crops/933/5.png	216	216	64	-120	正常	2026-06-08 17:09:25.480819	2026-06-08 17:09:25.480819	\N
83	1	6	枯草	0	0.00	/images/crops/1/9.png	211	233	41	3	枯萎	2026-06-08 16:50:46.784217	2026-06-08 16:50:46.784217	\N
76	9	6	枯草	0	0.00	/images/crops/basic/9.png	216	216	44	12	枯萎	2026-06-08 17:07:45.05785	2026-06-08 17:07:45.05785	\N
82	933	6	枯草	0	0.00	/images/crops/basic/9.png	216	216	46	-3	枯萎	2026-06-08 17:09:29.695886	2026-06-08 17:09:29.695886	\N
75	8	6	枯草	0	0.00	/images/crops/basic/9.png	208	208	49	4	枯萎	2026-06-08 17:08:01.142486	2026-06-08 17:08:01.142486	\N
77	13	6	枯草	0	0.00	/images/crops/basic/9.png	216	216	48	24	枯萎	2026-06-08 17:08:16.730099	2026-06-08 17:08:16.730099	\N
69	933	3	生长阶段	70	0.15	/images/crops/933/3.png	440	440	44	70	正常	2026-05-18 08:27:44.263901	2026-05-18 08:27:44.263901	\N
78	14	6	枯草	0	0.00	/images/crops/basic/9.png	216	216	34	0	枯萎	2026-06-08 17:08:30.205621	2026-06-08 17:08:30.205621	\N
79	30	6	枯草	0	0.00	/images/crops/basic/9.png	216	146	42	4	枯萎	2026-06-01 15:42:09.554598	2026-06-01 15:42:09.554598	\N
17	14	5	成熟阶段	10	0.05	/images/crops/14/5.png	249	263	15	-45	正常	2026-06-08 17:08:38.499174	2026-06-08 17:08:38.500255	\N
23	30	5	成熟阶段	10	0.05	/images/crops/30/5.png	212	142	36	-44	正常	2026-06-08 17:08:51.275137	2026-06-08 17:08:51.275137	\N
16	14	4	开花阶段	30	0.10	/images/crops/14/4.png	220	220	110	110	正常	2026-05-18 08:27:43.792327	2026-05-18 08:27:43.792327	\N
70	933	4	现蕾阶段	70	0.12	/images/crops/933/4.png	220	220	110	110	正常	2026-05-18 08:27:44.263901	2026-05-18 08:27:44.263901	\N
26	32	2	发芽阶段	80	0.10	/images/crops/32/2.png	352	352	66	70	正常	2026-05-18 08:27:43.881125	2026-05-18 08:27:43.881125	\N
27	32	3	生长阶段	90	0.15	/images/crops/32/3.png	440	440	44	70	正常	2026-05-18 08:27:43.881125	2026-05-18 08:27:43.881125	\N
61	418	1	种子阶段	60	0.05	/images/crops/basic/0.png	282	282	88	70	正常	2026-05-18 08:27:44.200984	2026-05-18 08:27:44.200984	\N
62	418	2	发芽阶段	80	0.10	/images/crops/418/2.png	352	352	66	70	正常	2026-05-18 08:27:44.200984	2026-05-18 08:27:44.200984	\N
73	6	6	枯草	0	0.00	/images/crops/basic/9.png	193	134	49	27	枯萎	2026-06-08 16:29:22.739264	2026-06-08 16:29:22.739264	\N
13	14	1	种子阶段	35	0.05	/images/crops/basic/0.png	282	282	88	70	正常	2026-05-18 08:27:43.792327	2026-05-18 08:27:43.792327	\N
14	14	2	发芽阶段	45	0.08	/images/crops/14/2.png	352	352	66	70	正常	2026-05-18 08:27:43.792327	2026-05-18 08:27:43.792327	\N
15	14	3	生长阶段	40	0.12	/images/crops/14/3.png	440	440	44	70	正常	2026-05-18 08:27:43.792327	2026-05-18 08:27:43.792327	\N
63	418	3	生长阶段	90	0.15	/images/crops/418/3.png	440	440	44	70	正常	2026-05-18 08:27:44.200984	2026-05-18 08:27:44.200984	\N
67	933	1	种子阶段	40	0.05	/images/crops/basic/0.png	282	282	88	70	正常	2026-05-18 08:27:44.263901	2026-05-18 08:27:44.263901	\N
28	32	4	开花阶段	70	0.12	/images/crops/32/4.png	216	146	73	0	正常	2026-06-01 17:10:19.010567	2026-06-01 17:10:19.010567	\N
29	32	5	成熟阶段	10	0.05	/images/crops/32/5.png	216	216	21	-94	正常	2026-06-08 17:08:59.696614	2026-06-08 17:08:59.696614	\N
\.


--
-- Data for Name: player; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.player (id, username, nickname, exp, points, gold, avatar_url, create_time, update_time) FROM stdin;
3	lvbu	吕布	1959	1258	821	/images/avatars/lvbu.jpg	2026-05-11 06:53:10.794485	2026-05-29 00:16:07.012847
4	zhugeliang	诸葛亮	205	230	615	/images/avatars/zhugeliang.png	2026-05-11 06:53:10.794485	2026-05-29 00:03:48.585737
2	caocao	曹操	595	530	891	/images/headImages/caocao.png	2026-05-11 06:53:10.794485	2026-05-28 23:08:00.971299
1	liubei	刘备	1076	512	3014	/images/avatars/liubei.jpg	2026-05-11 06:53:10.794485	2026-05-28 23:57:58.464087
\.


--
-- Data for Name: player_seed; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.player_seed (id, create_time, player_id, quantity, seed_id) FROM stdin;
3	2026-05-29 13:57:34.207831	1	0	7
18	2026-06-01 14:07:11.211203	2	0	8
5	2026-05-29 13:57:55.446895	1	0	32
7	2026-05-30 21:42:25.078559	1	0	418
6	2026-05-30 21:42:19.110249	1	0	30
16	2026-06-01 14:06:18.846853	2	0	1
17	2026-06-01 14:06:20.921602	2	0	6
4	2026-05-29 13:57:36.982352	1	1	8
2	2026-05-29 13:57:31.942756	1	0	6
1	2026-05-29 13:57:24.585603	1	3	1
10	2026-05-31 00:45:42.66155	3	0	7
19	2026-06-01 17:14:16.397135	3	0	418
20	2026-06-01 17:14:22.003598	3	0	933
21	2026-06-08 15:02:44.551498	3	0	940
8	2026-05-31 00:45:40.11733	3	1	1
9	2026-05-31 00:45:41.673189	3	9	6
11	2026-05-31 00:45:43.628507	3	1	8
22	2026-06-08 17:18:46.430449	3	1	9
13	2026-05-31 00:45:51.719722	3	1	13
12	2026-05-31 00:45:47.250763	3	1	14
23	2026-06-08 17:18:50.951592	3	1	30
24	2026-06-08 17:18:52.12131	3	1	32
15	2026-06-01 00:19:48.731073	4	0	6
14	2026-06-01 00:19:45.235001	4	0	1
\.


--
-- Name: farm_land_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.farm_land_id_seq', 96, true);


--
-- Name: growth_stage_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.growth_stage_id_seq', 100, true);


--
-- Name: player_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.player_id_seq', 5, true);


--
-- Name: player_seed_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.player_seed_id_seq', 24, true);


--
-- Name: seed_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seed_id_seq', 939, true);


--
-- PostgreSQL database dump complete
--

