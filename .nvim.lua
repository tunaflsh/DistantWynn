local root_dir = vim.uv.cwd()
if not root_dir then
  vim.notify("Failed to get the project's root directory", vim.log.levels.WARN)
  return
end

local project_name = vim.fn.fnamemodify(root_dir, ':t')
local hash = vim.fn.sha256(root_dir):sub(1, 16)
local project_id = project_name .. '-' .. hash

vim.o.shadafile = vim.fs.joinpath(vim.fn.stdpath('state'), 'shada', project_id .. '.shada')
