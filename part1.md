# 成果汇报：Unsloth 及相关依赖包安装

本次环境配置旨在为大型语言模型（LLM）的微调提供高效、低内存占用的支持，具体操作及成果如下：

## 1. 安装 Unsloth

- 通过 `pip install unsloth` 成功安装了 Unsloth 包，为微调 LLM 提供了高性能支持。
- Unsloth 的优势在于优化了训练速度和内存占用，适合大模型微调场景。

## 2. 从 GitHub 源码安装最新版 Unsloth

- 先卸载了当前环境中的 Unsloth 包，确保没有版本冲突。
- 通过如下命令从 GitHub 获取并安装了 Unsloth 的最新开发版：
  ```bash
  pip uninstall unsloth -y && pip install --upgrade --no-cache-dir --no-deps git+https://github.com/unslothai/unsloth.git
  ```
- 这样确保使用了最新的功能与 bug 修复，提高了后续开发与调试的灵活性。

## 3. 安装 bitsandbytes 与 unsloth_zoo

- 成功安装了 `bitsandbytes` 和 `unsloth_zoo` 两个包：
  - `bitsandbytes`：用于模型参数的8位量化与高效显存管理，有效降低了大模型的显存需求。
  - `unsloth_zoo`：提供了丰富的预训练模型和实用工具，便于快速开展模型微调工作。
## 4. 运行结果（部分展示）
![image](https://github.com/user-attachments/assets/d2a0544d-de1c-4932-bf6a-0083476adfb1)
## 总结

通过上述操作，当前环境已具备以 Unsloth 为核心的高效 LLM 微调能力，并兼容最新特性及相关依赖。后续可直接进行大模型的量化、加载和微调实验。

如有进一步的模型训练或优化需求，可基于当前环境无缝开展。
