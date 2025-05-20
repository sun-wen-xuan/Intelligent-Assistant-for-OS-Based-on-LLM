## 阶段一：环境与依赖搭建

- 目标：
  - 搭建稳定、高效的Notebook开发与实验环境。
  - 安装并校验核心依赖包（`unsloth`、`bitsandbytes`、`unsloth_zoo`等）。
- 主要成果：
  - 在Jupyter Notebook中完成环境初始化，采用命令输出隐藏技术（`%%capture`）提升可读性。
  - 通过pip自动化脚本，确保依赖始终为最新版，避免兼容性问题。
  - 检查NVIDIA驱动与CUDA环境，确认GPU可用，为后续大模型推理和训练做好准备。
- 代码：
 # 安装 unsloth 包。unsloth 是一个用于微调大型语言模型（LLM）的工具，可以让模型运行更快、占用更少内存。
!pip install unsloth

# 卸载当前已安装的 unsloth 包（如果已安装），然后从 GitHub 的源代码安装最新版本。
# 这样可以确保我们使用的是最新功能和修复。
!pip uninstall unsloth -y && pip install --upgrade --no-cache-dir --no-deps git+https://github.com/unslothai/unsloth.git

# 安装 bitsandbytes 和 unsloth_zoo 包。
# bitsandbytes 是一个用于量化和优化模型的库，可以帮助减少模型占用的内存。
# unsloth_zoo 可能包含了一些预训练模型或其他工具，方便我们使用。
!pip install bitsandbytes unsloth_zoo
- 展示运行结果：
  