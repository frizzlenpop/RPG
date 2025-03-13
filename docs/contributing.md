# Contributing Guidelines

Thank you for your interest in contributing to the RPG Skills Plugin! This document provides guidelines and instructions for contributing to the project.

## Ways to Contribute

There are many ways to contribute to the RPG Skills Plugin:

1. **Code Contributions** - Implementing new features or fixing bugs
2. **Documentation Improvements** - Enhancing or fixing documentation
3. **Bug Reports** - Reporting issues you encounter
4. **Feature Requests** - Suggesting new features or improvements
5. **Translations** - Helping translate the plugin into other languages
6. **Community Support** - Helping other users in the community

## Code Contributions

### Setting Up Your Development Environment

1. **Fork the Repository**:
   - Visit [the GitHub repository](https://github.com/frizzlenpop/RPGSkillsPlugin)
   - Click the "Fork" button in the top-right corner

2. **Clone Your Fork**:
   ```bash
   git clone https://github.com/yourusername/RPGSkillsPlugin.git
   cd RPGSkillsPlugin
   ```

3. **Set Up the Project**:
   ```bash
   mvn clean install
   ```

4. **Create a Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
   
   Use prefixes like:
   - `feature/` for new features
   - `bugfix/` for bug fixes
   - `docs/` for documentation changes
   - `refactor/` for code refactoring

### Coding Standards

Please follow these coding standards:

1. **Java Conventions**:
   - Use camelCase for variable and method names
   - Use PascalCase for class names
   - Use UPPERCASE_SNAKE_CASE for constants

2. **Documentation**:
   - Add JavaDoc comments to all public methods
   - Include parameter descriptions and return value information
   - Document complex algorithms with inline comments

3. **Code Structure**:
   - Keep methods small and focused
   - Follow SOLID principles
   - Use appropriate design patterns

4. **Naming**:
   - Use descriptive names for variables, methods, and classes
   - Avoid abbreviations unless they are widely understood

5. **Error Handling**:
   - Use appropriate exception handling
   - Log exceptions with useful context information
   - Validate input parameters

### Pull Request Process

1. **Keep Changes Focused**:
   - Each pull request should address a single concern
   - Avoid mixing unrelated changes

2. **Write a Good Commit Message**:
   - Start with a concise summary line (50 chars or less)
   - Follow with a detailed description if necessary
   - Reference issue numbers if applicable

3. **Update Documentation**:
   - Update any documentation affected by your changes
   - Include code examples for API changes

4. **Add Tests**:
   - Write tests for new features
   - Ensure existing tests pass with your changes

5. **Submit Your Pull Request**:
   - Push your changes to your fork
   - Create a pull request to the `develop` branch
   - Fill out the pull request template completely

6. **Code Review**:
   - Be responsive to feedback and questions
   - Make requested changes promptly
   - Discuss alternatives constructively

## Bug Reports and Feature Requests

### Reporting Bugs

When reporting bugs, please include:

1. **Description**: Clear and concise description of the issue
2. **Steps to Reproduce**: Detailed steps to reproduce the bug
3. **Expected Behavior**: What you expected to happen
4. **Actual Behavior**: What actually happened
5. **Environment**:
   - Minecraft version
   - Server type and version (Spigot, Paper, etc.)
   - Plugin version
   - Other plugins installed
6. **Screenshots/Logs**: If applicable, add screenshots or console logs

Use the "Bug Report" issue template when creating a new issue.

### Requesting Features

When requesting new features, please include:

1. **Description**: Clear and concise description of the feature
2. **Use Case**: Why this feature would be beneficial
3. **Proposed Implementation**: If you have ideas about how to implement it
4. **Alternatives Considered**: Any alternative solutions you've thought about
5. **Additional Context**: Any other relevant information

Use the "Feature Request" issue template when creating a new issue.

## Documentation Contributions

Documentation is just as important as code. To contribute to documentation:

1. **Identify Areas for Improvement**:
   - Missing information
   - Unclear explanations
   - Outdated content
   - Typos or grammatical errors

2. **Make Changes**:
   - Follow the same branch and PR process as code changes
   - Use clear, concise language
   - Include examples where appropriate

3. **Preview Changes**:
   - Ensure Markdown formatting is correct
   - Check that links work properly

## Translation Contributions

To help translate the plugin into other languages:

1. **Check Existing Translations**:
   - Look in the `src/main/resources/lang` directory

2. **Create or Update Translation Files**:
   - Copy `en_US.yml` to a new file named with the appropriate language code
   - Translate the strings in the file
   - Submit a pull request with your translation

3. **Review Guidelines**:
   - Keep formatting codes (like &a, &b, etc.)
   - Ensure placeholders (like %player%) remain intact
   - Maintain the same meaning in translations

## Community Guidelines

When participating in our community:

1. **Be Respectful**:
   - Treat all community members with respect
   - Disagree constructively
   - No harassment, discrimination, or offensive language

2. **Be Helpful**:
   - Answer questions when you can
   - Point people to relevant documentation
   - Share your knowledge and experience

3. **Stay On Topic**:
   - Keep discussions relevant to the plugin
   - Use appropriate channels for different topics

## Development Workflow

We follow a simplified GitFlow workflow:

1. **`main` Branch**:
   - Contains the latest stable release
   - Never commit directly to `main`

2. **`develop` Branch**:
   - Contains the latest development changes
   - Base your feature branches off `develop`

3. **Feature Branches**:
   - Named `feature/your-feature`
   - Merge back into `develop` when complete

4. **Release Process**:
   - `develop` is merged to `main` for releases
   - Tags are used to mark releases

## License

By contributing to the RPG Skills Plugin, you agree that your contributions will be licensed under the same [MIT License](../LICENSE.md) that covers the project.

## Recognition

Contributors are recognized in the following ways:

1. **Credits in Documentation**: Significant contributors are listed in the main documentation
2. **Commit History**: Your contributions are recorded in the Git history
3. **Release Notes**: Major contributions are acknowledged in release notes

## Getting Help

If you need help with contributing:

1. **Discord**: Join our [Discord server](https://discord.gg/rpgskills) for real-time help
2. **Issues**: Ask questions by creating an issue with the "question" label
3. **Discussions**: Use GitHub Discussions for longer-form questions

---

Thank you for contributing to the RPG Skills Plugin! Your efforts help make the plugin better for everyone. 