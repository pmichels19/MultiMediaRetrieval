from pathlib import Path


def reindex(file_path, update):
    # Open the .obj file for reading
    with open(file_path, 'r') as obj_file:
        lines = obj_file.readlines()
        obj_file.close()

    # Initialize a list to store the modified lines
    updated_lines = []

    # Iterate through the lines
    for line in lines:
        if line.startswith('f '):
            # Split the line into parts, update the indices, and join them back
            parts = line.split()
            updated_parts = ['f']
            for part in parts[1:]:
                vertex_indices = part.split('/')
                updated_indices = [str(int(idx) + update) for idx in vertex_indices]
                updated_parts.append('/'.join(updated_indices))
            updated_line = ' '.join(updated_parts) + '\n'
            updated_lines.append(updated_line)
        else:
            updated_lines.append(line)

    # Write the updated lines back to the .obj file
    with open(file_path, 'w') as obj_file:
        obj_file.writelines(updated_lines)
        obj_file.close()
        print(f"re-indexed {file_path}")


print("DO NOT RUN UNLESS ABSOLUTELY NECESSARY: CHECK IF ALL FILES NEED TO BE DONE OR ONLY ONE.")
# paths = Path("..\\ModelQueryTool\\Shapes").rglob("**/*_clean.obj")
# for path in paths:
#     reindex(str(path), 1)
