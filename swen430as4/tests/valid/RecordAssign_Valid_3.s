
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -8(%rbp)
	movq $2, %rbx
	movq %rbx, 8(%rax)
	movq $3, %rbx
	movq %rbx, 16(%rax)
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -16(%rbp)
	movq $1, %rbx
	movq %rbx, 8(%rax)
	movq $3, %rbx
	movq %rbx, 16(%rax)
	movq -8(%rbp), %rax
	movq $24, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $2, %rcx
	movq %rcx, 0(%rbx)
	movq $2, %rcx
	movq %rcx, 8(%rbx)
	movq $3, %rcx
	movq %rcx, 16(%rbx)
	jmp label636
	movq $1, %rax
	jmp label637
label636:
	movq $0, %rax
label637:
	movq %rax, %rdi
	call assertion
	movq -16(%rbp), %rax
	movq $24, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $2, %rcx
	movq %rcx, 0(%rbx)
	movq $1, %rcx
	movq %rcx, 8(%rbx)
	movq $3, %rcx
	movq %rcx, 16(%rbx)
	jmp label638
	movq $1, %rax
	jmp label639
label638:
	movq $0, %rax
label639:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq -16(%rbp), %rbx
	movq $1, %rax
	jmp label641
label640:
	movq $0, %rax
label641:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq $1, %rbx
	movq %rbx, 8(%rax)
	movq -8(%rbp), %rax
	movq $24, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $2, %rcx
	movq %rcx, 0(%rbx)
	movq $1, %rcx
	movq %rcx, 8(%rbx)
	movq $3, %rcx
	movq %rcx, 16(%rbx)
	jmp label642
	movq $1, %rax
	jmp label643
label642:
	movq $0, %rax
label643:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq -16(%rbp), %rbx
	jmp label644
	movq $1, %rax
	jmp label645
label644:
	movq $0, %rax
label645:
	movq %rax, %rdi
	call assertion
label635:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
