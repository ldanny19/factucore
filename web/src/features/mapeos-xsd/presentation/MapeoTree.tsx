import { useState } from "react";
import { Box, Checkbox, Collapse, IconButton, Typography } from "@mui/material";
import ChevronRightRoundedIcon from "@mui/icons-material/ChevronRightRounded";
import ExpandMoreRoundedIcon from "@mui/icons-material/ExpandMoreRounded";

export interface MapeoTreeNode {
  key: string;
  label: string;
  path?: string;
  children: MapeoTreeNode[];
  destinoKey?: string;
}

interface MapeoTreeProps {
  nodes: MapeoTreeNode[];
  selectedKeys: Set<string>;
  disabledKeys?: Set<string>;
  selectionMode: "cascade" | "single";
  onSelectionChange: (node: MapeoTreeNode, selected: boolean) => void;
}

interface MapeoTreeBranchProps extends Omit<MapeoTreeProps, "nodes"> {
  node: MapeoTreeNode;
  depth: number;
}

function obtenerClaves(node: MapeoTreeNode): string[] {
  return [node.key, ...node.children.flatMap(obtenerClaves)];
}

function MapeoTreeBranch({
  node,
  depth,
  selectedKeys,
  disabledKeys = new Set(),
  selectionMode,
  onSelectionChange,
}: MapeoTreeBranchProps) {
  const [expanded, setExpanded] = useState(depth === 0);
  const descendants = selectionMode === "cascade"
    ? obtenerClaves(node).filter((key) => !disabledKeys.has(key))
    : [node.key];
  const selectedCount = descendants.filter((key) => selectedKeys.has(key)).length;
  const checked = selectionMode === "single"
    ? selectedKeys.has(node.key)
    : descendants.length > 0 && selectedCount === descendants.length;
  const indeterminate = selectionMode === "cascade" && selectedCount > 0 && !checked;
  const disabled = selectionMode === "single"
    ? disabledKeys.has(node.key) || !node.destinoKey
    : descendants.length === 0;

  return (
    <Box sx={{ pl: depth === 0 ? 0 : 1.5 }}>
      <Box sx={{ display: "flex", alignItems: "center", minHeight: 34 }}>
        {node.children.length > 0 ? (
          <IconButton
            size="small"
            aria-label={expanded ? "Contraer" : "Expandir"}
            onClick={() => setExpanded((actual) => !actual)}
          >
            {expanded ? <ExpandMoreRoundedIcon fontSize="small" /> : <ChevronRightRoundedIcon fontSize="small" />}
          </IconButton>
        ) : <Box sx={{ width: 34 }} />}
        <Checkbox
          size="small"
          checked={checked}
          indeterminate={indeterminate}
          disabled={disabled}
          onChange={(_event, selected) => onSelectionChange(node, selected)}
          inputProps={{ "aria-label": node.label }}
        />
        <Typography variant="body2" sx={{ overflowWrap: "anywhere", wordBreak: "break-word" }}>
          {node.label}
        </Typography>
      </Box>
      {node.children.length > 0 && (
        <Collapse in={expanded} timeout="auto" unmountOnExit>
          {node.children.map((child) => (
            <MapeoTreeBranch
              key={child.key}
              node={child}
              depth={depth + 1}
              selectedKeys={selectedKeys}
              disabledKeys={disabledKeys}
              selectionMode={selectionMode}
              onSelectionChange={onSelectionChange}
            />
          ))}
        </Collapse>
      )}
    </Box>
  );
}

export default function MapeoTree(props: MapeoTreeProps) {
  return (
    <Box role="tree">
      {props.nodes.map((node) => (
        <MapeoTreeBranch
          key={node.key}
          node={node}
          depth={0}
          selectedKeys={props.selectedKeys}
          disabledKeys={props.disabledKeys}
          selectionMode={props.selectionMode}
          onSelectionChange={props.onSelectionChange}
        />
      ))}
    </Box>
  );
}
