"""Small hand-labeled evaluation set for rank_jds_for_student (matcher/ranking.py).

Not synthetic ground truth: STUDENT_JD_RELEVANCE was labeled by inspecting
each (student, jd) skill-list pair and judging relevance against the rule of
thumb below. Labeled by Claude at the repo owner's explicit request/rule of
thumb, not by the student/professor this data is meant to represent -- treat
this as a single (non-domain-expert) annotator's judgment, not a validated
ground truth, and revise any label that looks off on inspection.

relevance_label scale:
    0 - not relevant
    1 - somewhat relevant
    2 - strongly relevant
"""

# Each entry: (student_id, skills). Skills are drawn from
# data/skills_taxonomy.json so every entry is a valid canonical skill.
STUDENT_PROFILES = [
    ("student_fullstack", [
        "JavaScript", "TypeScript", "React", "Node.js", "Express.js",
        "MongoDB", "REST API", "Git",
    ]),
    ("student_ml", [
        "Python", "Machine Learning", "Deep Learning", "PyTorch",
        "Scikit-learn", "Pandas", "NumPy", "Natural Language Processing",
    ]),
    ("student_backend_java", [
        "Java", "Spring Boot", "Hibernate", "MySQL", "REST API",
        "Microservices", "JUnit", "Git",
    ]),
    ("student_cybersecurity", [
        "Network Security", "Penetration Testing", "Burp Suite", "Nmap",
        "Cryptography", "Vulnerability Assessment", "Linux",
    ]),
    ("student_devops_cloud", [
        "AWS", "Docker", "Kubernetes", "Terraform", "CI/CD",
        "Jenkins", "Linux", "System Design",
    ]),
]

# Each entry: (jd_id, skills). Skills are drawn from
# data/skills_taxonomy.json so every entry is a valid canonical skill.
JD_PROFILES = [
    ("jd_frontend_react", [
        "JavaScript", "TypeScript", "React", "Next.js", "REST API",
    ]),
    ("jd_fullstack_node", [
        "JavaScript", "Node.js", "Express.js", "MongoDB", "React", "Git",
    ]),
    ("jd_data_scientist", [
        "Python", "Machine Learning", "Pandas", "NumPy", "Scikit-learn",
    ]),
    ("jd_deep_learning_research", [
        "Python", "Deep Learning", "PyTorch", "Computer Vision",
        "Natural Language Processing",
    ]),
    ("jd_java_backend", [
        "Java", "Spring Boot", "Hibernate", "MySQL", "Microservices",
    ]),
    ("jd_security_analyst", [
        "Network Security", "SIEM", "Threat Intelligence",
        "Vulnerability Assessment", "Linux",
    ]),
    ("jd_penetration_tester", [
        "Penetration Testing", "Burp Suite", "Metasploit", "Nmap",
        "Cryptography",
    ]),
    ("jd_platform_devops", [
        "Docker", "Kubernetes", "Terraform", "AWS", "CI/CD", "Jenkins",
    ]),
]

# Hand-labeled by inspecting each student's skill list against each JD's
# skill list. Rule of thumb: 2 = strong overlap on the JD's core/defining
# skills, 1 = a single meaningful shared skill that's genuinely transferable
# (e.g. REST API design know-how, real Linux/systems familiarity) even
# though the core stack differs, 0 = different domain / no meaningful
# overlap. Generic tooling that's near-universal (e.g. Git alone) doesn't by
# itself bump a pair from 0 to 1.
STUDENT_JD_RELEVANCE: dict[str, list[tuple[str, int]]] = {
    "student_fullstack": [
        ("jd_frontend_react", 2),
        ("jd_fullstack_node", 2),
        ("jd_data_scientist", 0),
        ("jd_deep_learning_research", 0),
        ("jd_java_backend", 0),
        ("jd_security_analyst", 0),
        ("jd_penetration_tester", 0),
        ("jd_platform_devops", 0),
    ],
    "student_ml": [
        ("jd_frontend_react", 0),
        ("jd_fullstack_node", 0),
        ("jd_data_scientist", 2),
        ("jd_deep_learning_research", 2),
        ("jd_java_backend", 0),
        ("jd_security_analyst", 0),
        ("jd_penetration_tester", 0),
        ("jd_platform_devops", 0),
    ],
    "student_backend_java": [
        ("jd_frontend_react", 1),
        ("jd_fullstack_node", 0),
        ("jd_data_scientist", 0),
        ("jd_deep_learning_research", 0),
        ("jd_java_backend", 2),
        ("jd_security_analyst", 0),
        ("jd_penetration_tester", 0),
        ("jd_platform_devops", 0),
    ],
    "student_cybersecurity": [
        ("jd_frontend_react", 0),
        ("jd_fullstack_node", 0),
        ("jd_data_scientist", 0),
        ("jd_deep_learning_research", 0),
        ("jd_java_backend", 0),
        ("jd_security_analyst", 2),
        ("jd_penetration_tester", 2),
        ("jd_platform_devops", 0),
    ],
    "student_devops_cloud": [
        ("jd_frontend_react", 0),
        ("jd_fullstack_node", 0),
        ("jd_data_scientist", 0),
        ("jd_deep_learning_research", 0),
        ("jd_java_backend", 0),
        ("jd_security_analyst", 1),
        ("jd_penetration_tester", 0),
        ("jd_platform_devops", 2),
    ],
}
