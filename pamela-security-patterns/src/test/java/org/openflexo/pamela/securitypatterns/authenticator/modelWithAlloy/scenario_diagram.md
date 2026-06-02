```tikz
\usepackage{tikz}
\usetikzlibrary{er}
\usetikzlibrary{positioning}
\usetikzlibrary{matrix}
\usetikzlibrary{trees}
\usetikzlibrary{mindmap}
\usetikzlibrary{arrows.meta, positioning, shapes.geometric}
\begin{document}
\begin{tikzpicture}[
  mindmap,
  grow cyclic,
  every node/.style={concept, execute at begin node=\hskip0pt, align=flush center},
 root concept/.append style={
    concept color=blue!25, 
    text=black, 
    minimum size=3.5cm,
    font=\large
  },
  level 1 concept/.append style={
    concept color=teal!55, 
    text=black, 
    minimum size=2.8cm,
    level distance=5cm,
    sibling angle=120
  },
  level 2 concept/.append style={
    concept color=orange!65, 
    text=black, 
    minimum size=2.2cm,
    level distance=4.5cm,
    sibling angle=90
  },
  level 3 concept/.append style={
    concept color=green!55, 
    text=black, 
    minimum size=1.8cm,
    level distance=3.5cm,
    font=\small
  },
  level 4 concept/.append style={
    concept color=blue!45, 
    text=black, 
    minimum size=1.4cm,
    level distance=3cm,
    font=\footnotesize
  },
  concept color=blue!25
]

\node[root concept] {Synchronisation de modele}
    child { node {Quoi}
      child { node {Acteur}
        child { node {Expert modele} }
        child { node {Expert securite} }
        child { node {Expert domaine} }
      }
      child { node {Activite}
        child { node {Conception du systeme} }
        child { node {Ajout des annotations} }
        child { node {Observation du comportement} }
        child { node {Modification / adaptation} }
        child { node {Partage / Distribution} }
      }
      child { node {Artefact}
        child { node {Annotation de Securite}
          child { node {Authenticator} }
        }
        child[grow=35, level distance=5.0cm] { node[text width=2.2cm] {Modele}
          child[sibling angle=0, level distance=4.2cm] { node[text width=2.0cm] {Systeme}
            child[level distance=2.2cm] { node[text width=1.9cm] {Utilisateur, Livre, ...} }
            child[level distance=4.2cm] { node[text width=1.9cm] {expert domaine + modele + securite} }
          }
          child[sibling angle=90, level distance=4.2cm] { node[text width=2.0cm] {Meta-systeme}
            child[level distance=6.2cm] { node[text width=1.9cm] {Attribut, Methode, Classe} }
            child[level distance=8.2cm] { node[text width=1.9cm] {expert modele + securite} }
          }
          child[sibling angle=190, level distance=4.2cm] { node[text width=2.0cm] {Meta-meta-systeme}
            child[level distance=10.2cm] { node[text width=1.9cm] {Objet, Relation...} }
            child[level distance=3.2cm] { node[text width=1.9cm] {expert modele + securite} }
          }
        }
      }
      child { node {Resultat}
        child { node {Modeles} }
        child { node {Observations} }
        child { node {Traces} }
      }
    };

\end{tikzpicture}
\end{document}
```