#!/usr/bin/env python3
"""
需求文档验证脚本
验证所有需求文档的完整性、一致性和格式正确性
"""

import os
import re
import sys
from pathlib import Path

def check_file_exists(filepath):
    """检查文件是否存在"""
    if not os.path.exists(filepath):
        print(f"❌ 文件不存在: {filepath}")
        return False
    print(f"✅ 文件存在: {filepath}")
    return True

def check_markdown_structure(filepath, expected_sections):
    """检查Markdown文档结构"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 检查主要章节
    sections = re.findall(r'^##\s+', content, re.MULTILINE)

    if len(sections) >= expected_sections:
        print(f"✅ {filepath}: 包含{len(sections)}个主要章节（期望至少{expected_sections}个）")
        return True
    else:
        print(f"❌ {filepath}: 只包含{len(sections)}个主要章节（期望至少{expected_sections}个）")
        return False

def check_user_stories_references():
    """检查用户故事引用一致性"""
    print("\n🔍 检查用户故事引用一致性...")

    # 检查USER_STORIES.md包含所有用户故事
    with open('prompts/requirements/USER_STORIES.md', 'r', encoding='utf-8') as f:
        us_content = f.read()

    us_stories = re.findall(r'US-\d{3}', us_content)
    print(f"USER_STORIES.md包含{len(set(us_stories))}个用户故事: {', '.join(sorted(set(us_stories)))}")

    # 检查每个模块文档是否正确引用用户故事
    modules = {
        'USER_MANAGEMENT.md': ['US-001', 'US-002', 'US-003'],
        'DEPARTMENT_MANAGEMENT.md': ['US-004', 'US-005'],
        'ROLE_PERMISSION_MANAGEMENT.md': ['US-006', 'US-007', 'US-008'],
        'AUDIT_LOG.md': ['US-009', 'US-010'],
        'AUTHENTICATION_AUTHORIZATION.md': ['US-011', 'US-012']
    }

    all_passed = True
    for module_file, expected_stories in modules.items():
        filepath = f"prompts/requirements/{module_file}"
        if os.path.exists(filepath):
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()

            missing_stories = []
            for story in expected_stories:
                if story not in content:
                    missing_stories.append(story)

            if not missing_stories:
                print(f"✅ {module_file}: 正确引用所有期望的用户故事")
            else:
                print(f"❌ {module_file}: 缺少用户故事引用: {', '.join(missing_stories)}")
                all_passed = False
        else:
            print(f"⚠️  文件不存在: {filepath}")
            all_passed = False

    return all_passed

def check_nfr_coverage():
    """检查非功能需求覆盖"""
    print("\n🔍 检查非功能需求覆盖...")

    with open('prompts/requirements/NON_FUNCTIONAL_REQUIREMENTS.md', 'r', encoding='utf-8') as f:
        nfr_content = f.read()

    # 检查NFR文档的主要章节
    nfr_sections = ['性能需求', '安全需求', '技术架构需求', '可用性与兼容性', '扩展性需求', '维护与运维']

    all_passed = True
    for section in nfr_sections:
        if f"## {section}" in nfr_content:
            print(f"✅ NON_FUNCTIONAL_REQUIREMENTS.md包含: {section}")
        else:
            print(f"❌ NON_FUNCTIONAL_REQUIREMENTS.md缺少: {section}")
            all_passed = False

    return all_passed

def main():
    """主验证函数"""
    print("🔍 开始需求文档验证...\n")

    # 检查所有必需文件是否存在
    required_files = [
        'prompts/requirements/NON_FUNCTIONAL_REQUIREMENTS.md',
        'prompts/requirements/USER_STORIES.md',
        'prompts/requirements/USER_MANAGEMENT.md',
        'prompts/requirements/DEPARTMENT_MANAGEMENT.md',
        'prompts/requirements/ROLE_PERMISSION_MANAGEMENT.md',
        'prompts/requirements/AUDIT_LOG.md',
        'prompts/requirements/AUTHENTICATION_AUTHORIZATION.md',
        'prompts/requirements/SYSTEM_CONFIGURATION.md'
    ]

    print("📁 检查文件存在性...")
    files_exist = all(check_file_exists(f) for f in required_files)

    # 检查文档结构
    print("\n📋 检查文档结构...")
    structure_checks = [
        ('prompts/requirements/NON_FUNCTIONAL_REQUIREMENTS.md', 6),
        ('prompts/requirements/USER_STORIES.md', 2),
        ('prompts/requirements/USER_MANAGEMENT.md', 6),
        ('prompts/requirements/DEPARTMENT_MANAGEMENT.md', 6),
        ('prompts/requirements/ROLE_PERMISSION_MANAGEMENT.md', 6),
        ('prompts/requirements/AUDIT_LOG.md', 6),
        ('prompts/requirements/AUTHENTICATION_AUTHORIZATION.md', 6),
        ('prompts/requirements/SYSTEM_CONFIGURATION.md', 5)
    ]

    structure_passed = all(check_markdown_structure(f, s) for f, s in structure_checks)

    # 检查一致性
    user_stories_passed = check_user_stories_references()
    nfr_passed = check_nfr_coverage()

    # 总结
    print("\n" + "="*50)
    print("📊 验证总结:")
    print("="*50)
    print(f"文件存在检查: {'✅ 通过' if files_exist else '❌ 失败'}")
    print(f"文档结构检查: {'✅ 通过' if structure_passed else '❌ 失败'}")
    print(f"用户故事引用: {'✅ 通过' if user_stories_passed else '❌ 失败'}")
    print(f"非功能需求覆盖: {'✅ 通过' if nfr_passed else '❌ 失败'}")

    all_passed = files_exist and structure_passed and user_stories_passed and nfr_passed

    if all_passed:
        print("\n🎉 所有验证通过！需求文档创建完成。")
        return 0
    else:
        print("\n❌ 验证失败，请检查上述问题。")
        return 1

if __name__ == '__main__':
    sys.exit(main())
