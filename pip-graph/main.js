import * as THREE from 'three';
import ForceGraph3D from '3d-force-graph';
import { UnrealBloomPass } from 'three/examples/jsm/Addons.js';

const inspector = document.getElementById('inspector');
const container = document.getElementById('graph');
const inspectorTitle = document.getElementById('inspector-title');
const inspectorContent = document.getElementById('inspector-content');
const inspectorClearBtn = document.getElementById('inspector-clear');

const typeColors = {
    METHOD: 'rgb(55, 0, 255)',
    CLASS: 'rgb(199, 0, 0)',
    default: '#cccccc'
};
  
const linkColors = {
    CONTAINS: 'rgb(255, 74, 195)',
    INVOKES: 'rgb(86, 255, 95)',
    default: 'rgba(180,180,200,0.4)'
};

const Graph = ForceGraph3D()(document.getElementById('graph'))
  .backgroundColor('#111')
  .nodeColor(n => typeColors[n.type] || typeColors.default)
  .nodeVal(n => {
    switch (n.type) {
      case 'CLASS': return 10;
      case 'METHOD': return 6;
      default: return 4;
    }
  })
  .linkColor(l => linkColors[l.type] || linkColors.default)
  .linkWidth(l => {
    if (l.weight) return Math.max(0.5, Math.min(4, l.weight));
    return l.type === 'CONTAINS' ? 2 : 1;
  })
  .linkDirectionalParticles(l => {
    return l.type === 'INVOKES' ? 5 : 0;
  })
  .linkDirectionalParticleSpeed(l => l.type === 'INVOKES' ? 0.01 : 0.006)
  .linkDirectionalParticleWidth(l => l.type === 'INVOKES' ? 2 : 0)
  .linkDirectionalArrowLength(5)
  .nodeRelSize(4)
  .nodeOpacity(1)
  .linkOpacity(0.6)
  .warmupTicks(120)
  .cooldownTime(8000)
  .onNodeClick(node => {
    // Aim at node from outside it
    const distance = 40;
    const distRatio = 1 + distance/Math.hypot(node.x, node.y, node.z);

    const newPos = node.x || node.y || node.z
      ? { x: node.x * distRatio, y: node.y * distRatio, z: node.z * distRatio }
      : { x: 0, y: 0, z: distance }; // special case if node is in (0,0,0)

    Graph.cameraPosition(
      newPos, // new position
      node, // lookAt ({ x, y, z })
      3000  // ms transition duration
    );
    showInspector('node', node);
});

const bloomPass = new UnrealBloomPass();
bloomPass.strength = 3;
bloomPass.threshold = 0.85;
bloomPass.radius = 0.25;

Graph.postProcessingComposer().addPass(bloomPass);

Graph.renderer().setClearColor(0x0b0e13, 1);

// Create a starfield Group so it's easy to manage
const starfield = new THREE.Group();
Graph.scene().add(starfield);

// Parameters
const STAR_RADIUS = 5000;    // radius of the star shell
const STAR_COUNT = 4000;     // number of stars (adjust for performance/visuals)
const STAR_SIZE = 1.2;       // pixel size of stars

// Geometry: random points on a sphere with slight radial jitter
const positions = new Float32Array(STAR_COUNT * 3);
for (let i = 0; i < STAR_COUNT; i++) {
  const u = Math.random();
  const v = Math.random();
  const theta = 2 * Math.PI * u;
  const phi = Math.acos(2 * v - 1);

  const r = STAR_RADIUS * (0.98 + 0.04 * Math.random()); // tiny jitter
  const x = r * Math.sin(phi) * Math.cos(theta);
  const y = r * Math.sin(phi) * Math.sin(theta);
  const z = r * Math.cos(phi);

  positions[i * 3 + 0] = x;
  positions[i * 3 + 1] = y;
  positions[i * 3 + 2] = z;
}

const starGeo = new THREE.BufferGeometry();
starGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));

// Soft, slightly bluish-white stars
const starMat = new THREE.PointsMaterial({
  color: 0xCFE8FF,
  size: STAR_SIZE,
  sizeAttenuation: true,
  transparent: true,
  opacity: 0.95,
  depthWrite: false // prevents z-fighting haze
});

const stars = new THREE.Points(starGeo, starMat);
starfield.add(stars);

// Optional: very faint nebula gradient sphere behind stars for richness
const nebula = new THREE.Mesh(
  new THREE.SphereGeometry(STAR_RADIUS * 0.999, 32, 32),
  new THREE.MeshBasicMaterial({
    side: THREE.BackSide,
    color: 0x06080f,
    transparent: true,
    opacity: 0.9
  })
);
starfield.add(nebula);

// Optional gentle parallax: rotate starfield slowly
function animateStarfield() {
  starfield.rotation.y += 0.00005;
  starfield.rotation.x += 0.00002;
  requestAnimationFrame(animateStarfield);
}
animateStarfield();

const ro = new ResizeObserver(entries => {
  for (const entry of entries) {
    const cr = entry.contentRect;
    applySize(Math.floor(cr.width), Math.floor(cr.height));
  }
});
ro.observe(container);

// Initial size
applySize(container.clientWidth, container.clientHeight);

// Optional: clean up on hot reload or page unload
window.addEventListener('beforeunload', () => ro.disconnect());

(async function init() {
    try {
        const data = await loadJSON('./input.json');
        const idSet = new Set(data.nodes.map(n => n.id));
        data.links = data.links.filter(l => idSet.has(l.source) && idSet.has(l.target));
        Graph.graphData(data);
    } catch (e) {
        console.error(e);
        err.textContent = `Failed to load input.json: ${e.message}`;
    }
})();

async function loadJSON(url) {
    const res = await fetch(url, { cache: 'no-cache' });
    console.log(res);
    if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
    return res.json();
}

function showInspector(kind, obj) {
    inspectorTitle.textContent = kind === 'node' ? 'Node' : 'Link';
    inspectorContent.textContent = prettyPrint(obj);
}
  
function clearInspector() {
    inspectorTitle.textContent = 'Inspector';
    inspectorContent.textContent = '{ click a node or link }';
}
  
inspectorClearBtn.addEventListener('click', clearInspector);
  
function prettyPrint(val) {
  return `id: ${val.id}\n` +
          `type: ${val.type}\n` +
          `name: ${val.name}\n` +
          `fqName: ${val.fqName}\n` +
          `filePath: ${val.filePath}\n` +
          `line: ${val.lineNumber}\n` +
          `code: ${val.type === "METHOD" ? val.code : "CLASS CODE NOT SAVED" }\n`;
}

function applySize(width, height) {
  if (!width || !height) return;
  Graph.width(width);
  Graph.height(height);

  const r = Math.min(window.devicePixelRatio || 1, 2);
  Graph.renderer().setPixelRatio(r);

  const camera = Graph.camera();
  camera.aspect = width / height;
  camera.updateProjectionMatrix();

  const comp = Graph.postProcessingComposer?.();
  if (comp && comp.setSize) comp.setSize(width, height);
}